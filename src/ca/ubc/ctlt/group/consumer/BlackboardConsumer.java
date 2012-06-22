package ca.ubc.ctlt.group.consumer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import blackboard.base.BaseComparator;
import blackboard.base.GenericFieldComparator;
import blackboard.data.ValidationException;
import blackboard.data.course.CourseMembership;
import blackboard.data.course.Group;
import blackboard.data.course.GroupMembership;
import blackboard.data.user.User;
import blackboard.persist.BbPersistenceManager;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.persist.course.GroupDbLoader;
import blackboard.persist.course.GroupDbPersister;
import blackboard.persist.course.GroupMembershipDbPersister;
import blackboard.persist.user.UserDbLoader;
import blackboard.platform.context.Context;
import blackboard.platform.persistence.PersistenceServiceFactory;

import ca.ubc.ctlt.group.Consumer;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class BlackboardConsumer extends Consumer {
	private BbPersistenceManager bbPm = PersistenceServiceFactory.getInstance()
			.getDbPersistenceManager();
	private int numGroups = 0;
	private Group lastGroup = null;
	
	public void goodbye(Id courseId) throws IOException {
		if (lastGroup == null)
		{ // do nothing if an error happened
			return;
		}
		if (numGroups == 1)
		{
			String url = "/webapps/blackboard/execute/modulepage/viewGroup?editMode=true&course_id=" + courseId.getExternalString() + 
					"&group_id=" + lastGroup.getId().getExternalString();
			response.sendRedirect(url);
		}
		else
		{
			String url = "/webapps/blackboard/execute/groupInventoryList?course_id=" + courseId.getExternalString();
			response.sendRedirect(url);
		}
		numGroups = 0;
		lastGroup = null;
	}

	@Override
	public void setGroupSets(HashMap<String, GroupSet> sets) throws Exception {
		if (sets == null) {
			error("Group is empty!");
			throw new NullPointerException("Group is empty!");
		}
		
		Context ctx = new BlackboardUtil(request).getContext();

		log("Initializing loaders...");
		GroupDbPersister groupDbPersister = null;
		GenericFieldComparator<Group> groupTitleComparator = new GenericFieldComparator<Group>(
				BaseComparator.ASCENDING, "getTitle", Group.class);

		List<Group> courseGroups = null;
		GroupDbLoader groupLoader = (GroupDbLoader) bbPm
				.getLoader(GroupDbLoader.TYPE);
		courseGroups = groupLoader.loadByCourseId(ctx.getCourseId());
		groupDbPersister = (GroupDbPersister) bbPm
				.getPersister(GroupDbPersister.TYPE);

		Collections.sort(courseGroups, groupTitleComparator);

		log("Loading group sets from course " + ctx.getCourseId());
		List<Group> bbGroupSets = blackboard.persist.course.impl.GroupDAO.get()
				.loadGroupSetsOnly(ctx.getCourseId());

		for (Entry<String, GroupSet> entry : sets.entrySet()) {
			GroupSet set = entry.getValue();
			Group bbGroupSet = new Group();
			bbGroupSet.setId(null);
			bbGroupSet.setGroupSet(true);
			bbGroupSet.setTitle(set.getName());
			bbGroupSet.setCourseId(ctx.getCourseId());
			
			log("Processing group set: " + set.getName());

			if (!set.getName().equals(GroupSet.EMPTY_NAME)) {
				// find if the group set exists
				for (Group s : bbGroupSets) {
					if (s.getTitle().equals(set.getName())) {
						bbGroupSet = s;
					}
				}

				// if not, create it
				if (bbGroupSet.getId() == null) {
					groupDbPersister.persist(bbGroupSet);
				}
			}

			for (Entry<String, ca.ubc.ctlt.group.Group> entry1 : set
					.getGroups().entrySet()) {
				ca.ubc.ctlt.group.Group group = entry1.getValue();

				log("Processing group: " + group.getName());
				
				// creating or getting group from database
				Group bbGroup = new Group();
				bbGroup.setTitle(group.getName());
				int courseGroupIndex = Collections.binarySearch(courseGroups,
						bbGroup, groupTitleComparator);
				if (courseGroupIndex < 0) {
					// group doesn't exist, create it
					try {
						bbGroup = createGroup(group, bbGroupSet);
					} catch (ValidationException e) {
						error("Failed to create group: "+group.getName()+". ("+e.getMessage()+")");
						continue;
					}
				} else {
					bbGroup = (Group) courseGroups.get(courseGroupIndex);
				}
				numGroups++;
				lastGroup = bbGroup;
				
				// creating members in the group
				createMembership(bbGroup, group.getMemberList());
			}
		}
		
		goodbye(ctx.getCourseId());
	}
	
	private Group createGroup(ca.ubc.ctlt.group.Group group, Group groupSet) throws PersistenceException, ValidationException {
		GroupDbPersister groupDbPersister;
		
		groupDbPersister = (GroupDbPersister) bbPm.getPersister(GroupDbPersister.TYPE);
		
		// create the group
		Group bbGroup = new Group();
		bbGroup.setTitle(group.getName());
		bbGroup.setCourseId(groupSet.getCourseId());
		bbGroup.setSetId(groupSet.getId());
		groupDbPersister.persist(bbGroup);
		
		return bbGroup;
	}
	
	private void createMembership(Group bbGroup, HashMap<String, ca.ubc.ctlt.group.User> memberList) throws PersistenceException {
		GroupMembershipDbPersister groupMembershipDbPersister;
		CourseMembershipDbLoader courseMembershipLoader;
		UserDbLoader userLoader;
		
		userLoader = (UserDbLoader) bbPm.getLoader(UserDbLoader.TYPE);
		groupMembershipDbPersister = (GroupMembershipDbPersister) bbPm
				.getPersister(GroupMembershipDbPersister.TYPE);
		courseMembershipLoader = (CourseMembershipDbLoader) bbPm
				.getLoader(CourseMembershipDbLoader.TYPE);
		
		// create membership
		for (Entry<String, ca.ubc.ctlt.group.User> entry : memberList.entrySet()) {
			ca.ubc.ctlt.group.User user = entry.getValue();
			User bbUser = null;
			
			try {
				bbUser = userLoader.loadByUserName(user.getId());

				CourseMembership courseMembership = courseMembershipLoader
						.loadByCourseAndUserId(bbGroup.getCourseId(),
								bbUser.getId());
				GroupMembership groupMembership = new GroupMembership();
				groupMembership.setCourseMembershipId(courseMembership.getId());
				groupMembership.setGroupId(bbGroup.getId());

				groupMembershipDbPersister.persist(groupMembership);
			} catch (Exception e) {
				error("Failed to save user " + bbUser.getId()
						+ "in group " + bbGroup.getTitle() + "( "
						+ e.getMessage() + ")");
			}
		}
	}

	@Override
	public String getOptionsPage()
	{
		return "consumers/blackboard/options.html";
	}

	@Override
	public String getName()
	{
		return "Blackboard";
	}

	@Override
	public String getDescription()
	{
		return "Creating group for Blackboard Learn.";
	}
}
