package ca.ubc.ctlt.group.blackboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;

import javax.servlet.http.HttpServletRequest;

import blackboard.base.InitializationException;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.BbServiceException;
import blackboard.platform.BbServiceManager;
import blackboard.platform.context.Context;
import blackboard.platform.context.ContextManager;
import blackboard.platform.db.JdbcServiceFactory;
import blackboard.platform.log.LogService;
import blackboard.platform.log.LogServiceFactory;
import ca.ubc.ctlt.group.GroupSet;

public class BlackboardUtil
{
	private Context ctx;
	private static final LogService LOG = LogServiceFactory.getInstance();
	private Connection db;

	private static Context extractContext(HttpServletRequest request) {
		ContextManager ctxMgr = null;
		Context context = null;
		try {
			// get services
			LOG.logDebug("Initializing context manager...");
			ctxMgr = (ContextManager) BbServiceManager
					.lookupService(ContextManager.class);
			context = ctxMgr.setContext(request);
			LOG.logDebug("Current context: " + context);
		} catch (BbServiceException e) {
			LOG.logFatal("Lookup service failed! " + e.getMessage(), e);
		} catch (InitializationException e) {
			LOG.logFatal(
					"Failed to initialize the context manager! "
							+ e.getFullMessageTrace(), e);
		} finally {
			if (ctxMgr != null) {
				ctxMgr.releaseContext();
			}
		}

		return context;
	}

	public BlackboardUtil(Context ctx) {
		this(ctx, null);
	}

	public BlackboardUtil(HttpServletRequest request) {
		this(BlackboardUtil.extractContext(request));
	}

	public BlackboardUtil(HttpServletRequest request, Connection db) {
		this(BlackboardUtil.extractContext(request), db);
	}

	public BlackboardUtil(Context ctx, Connection db) {
		this.ctx = ctx;

		if (null != db) {
			this.db = db;
		} else {
			try {
				this.db = JdbcServiceFactory.getInstance().getDefaultDatabase()
						.getConnectionManager().getConnection();
			} catch (ConnectionNotAvailableException e) {
				LOG.logError("Could not get DB connection!", e);
			}
		}
	}

	public Context getContext() {
		return ctx;
	}
	
	public Map<String, GroupSet> getGroupSets() throws PersistenceException
	{
		HashMap<String, GroupSet> sets;
		
		sets = getSets(ctx.getCourseId());
		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
		GroupSet defaultSet = new GroupSet(GroupSet.EMPTY_NAME);
		
		for(Group s: bbGroups) {
			LOG.logDebug("Processing bbGroup: " + s);
			ca.ubc.ctlt.group.GroGroup g = new ca.ubc.ctlt.group.GroGroup(s);
			if (!s.isInGroupSet()) {
				defaultSet.addGroup(g);
				LOG.logDebug("Added to default set");
			} else {
				boolean added = false;
				for (Entry<String, GroupSet> entry : sets.entrySet()) {
					GroupSet set = entry.getValue();
					if (set.getId().equals(s.getSetId().toExternalString())) {
						set.addGroup(g);
						added = true;
						LOG.logDebug("Added to "+ set.getName() +" set");
					}
				}
				
				if (!added) {
					throw new MissingResourceException("Group " + g.getName() + " could not find group set!", "GroupSet", g.getName());
				}
			}
		}
		
		if (!defaultSet.getGroups().isEmpty()) {
			sets.put(GroupSet.EMPTY_NAME, defaultSet);
		}
		LOG.logDebug(sets.toString());
		return sets;
	}
	
	/**
	 * Filter out Group objects that actually represent groupsets and return the list of groups
	 * that are actualy groups.
	 * @return
	 * @throws PersistenceException
	 */
	public List<Group> getGroups() throws PersistenceException {
		List<Group> ret = new ArrayList<Group>();
		
		List<Group> bbGroups = getAllBbGroups(ctx.getCourseId());
		
		for(Group group : bbGroups) {
			if (!group.isGroupSet()) {
				ret.add(group);
			}
		}
		
		return ret;
	}
	
	protected HashMap<String, GroupSet> getSets (Id courseId) {
		HashMap<String, GroupSet> sets = new HashMap<String, GroupSet>();
		
		//log("Loading group sets from course " + courseId);
		List<Group> bbGroupSets = blackboard.persist.course.impl.GroupDAO.get()
				.loadGroupSetsOnly(courseId);
		
		if (bbGroupSets.isEmpty()) {
			return sets;
		}
		
		for (Group s : bbGroupSets) {
			GroupSet set = new GroupSet(s.getTitle());
			set.setId(s.getId().toExternalString());
			sets.put(s.getTitle(), set);
		}
	
		return sets;
	}
	
	public List<Group> getAllBbGroups(Id courseId) throws PersistenceException {
		List<Group> courseGroups = null;
		GroupDbLoader groupLoader =  GroupDbLoader.Default.getInstance();
		courseGroups = groupLoader.loadByCourseId(courseId);
		return courseGroups;
	}
	
	public List<User> getUsers(String groupId) throws PersistenceException, ConnectionNotAvailableException {
		// To get a list of users in a group, we have to traverse a hierarchy of data relations.
		// The hierarchy goes Group -> GroupMembership -> CourseMembership -> User
		if (groupId.isEmpty()) {
			return new ArrayList<User>();
		}
		
		// First, we get the Group
		GroupDbLoader gLoader = GroupDbLoader.Default.getInstance();
		Group group = gLoader.loadById(Id.generateId(Group.DATA_TYPE, groupId));
		// Then we get the list of GroupMemberships in the Group
		List<GroupMembership> groupMembers = group.getGroupMemberships();
		
		// From each GroupMembership, we can get the CourseMembership
		CourseMembershipDbLoader cmLoader = CourseMembershipDbLoader.Default.getInstance();
		ArrayList<CourseMembership> courseMembers = new ArrayList<CourseMembership>();
		for (GroupMembership gmember : groupMembers) {
			CourseMembership cmember = cmLoader.loadById(gmember.getCourseMembershipId(), db, true);
			courseMembers.add(cmember);
		}
		
		// Then, finally, from the CourseMembership, we can get the Users
		ArrayList<User> ret = new ArrayList<User>();
		for (CourseMembership member : courseMembers) {
			ret.add(member.getUser());
		}
		
		try {
			db.close();
		} catch (SQLException e) {
			LOG.logDebug("Failed to close the database connection!", e);
		}
		return ret;
	}
	
	public User findUserByUsername(String username) {
		User user = null;
		try {
			user = UserDbLoader.Default.getInstance().loadByUserName(username, db, true);
		} catch (KeyNotFoundException e) {
			LOG.logError("User with username "+username+" cannot be found!", e);
		} catch (PersistenceException e) {
			LOG.logError("Reading database error!", e);
		}
		
		return user;
	}
	
	/**
	 * Given a student id, load the user object by searching the class list for
	 * a user with the matching student id.
	 * 
	 * @param studentId
	 */
	public User findUserByStudentId(String studentId) {
		User user = null;

		try {
			ArrayList<User> users = UserDbLoader.Default.getInstance()
					.loadByCourseId(ctx.getCourseId(), db, true);

			for (User u : users) {
				if (u.getStudentId().equals(studentId)) {
					user = u;
					break;
				}
			}
		} catch (PersistenceException e) {
			LOG.logError("Reading database error!", e);
		}

		return user;
	}
}
