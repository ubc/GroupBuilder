package ca.ubc.ctlt.group.consumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import blackboard.base.FormattedText;
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
import blackboard.persist.course.GroupMembershipDbLoader;
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

	private GroupDbLoader groupLoader;
	private GroupDbPersister groupPersister;
	private GroupMembershipDbPersister groupMembershipPersister;

	// Username String to User object
	private HashMap<String, User> users = new HashMap<String, User>();
	// Username String to course membership
	private HashMap<Id, CourseMembership> courseMemberships = new HashMap<Id, CourseMembership>();
	// Group name to Group object
	private HashMap<String, Group> groups = new HashMap<String, Group>();
	// CourseMembership id to hash of Group Id to GroupMembership
	private HashMap<Id, HashMap<Id, GroupMembership>> groupMemberships = new HashMap<Id, HashMap<Id, GroupMembership>>();

	/**
	 * If only 1 group was created, redirect user to that group's view page. If multiple groups
	 * were created, redirect user to the groups list page.
	 *
	 * @param courseId - the course that the groups just created live in.
	 * @throws IOException - Redirect failed
	 */
	public void goodbye(Id courseId) throws IOException
	{
		if (lastGroup == null || !errors.isEmpty())
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
			String url = "/webapps/bb-group-mgmt-LEARN/execute/groupContentList?course_id=" + courseId.getExternalString();
			response.sendRedirect(url);
		}
		numGroups = 0;
		lastGroup = null;
	}

	/**
	 * Attempt to speed up import by doing all the required reading from
	 * database up front. This avoids having a lot of small database queries
	 * that are run for every single user in a group.
	 *
	 * @throws PersistenceException
	 */
	private void initLoaders(Context ctx) throws PersistenceException
	{
		groupLoader = GroupDbLoader.Default.getInstance();
		UserDbLoader userLoader = UserDbLoader.Default.getInstance();
		CourseMembershipDbLoader courseMembershipLoader = CourseMembershipDbLoader.Default.getInstance();
		GroupMembershipDbLoader groupMembershipLoader = GroupMembershipDbLoader.Default.getInstance();
		groupPersister = GroupDbPersister.Default.getInstance();
		groupMembershipPersister = GroupMembershipDbPersister.Default.getInstance();

		// create user mappings for quick lookup later
		HashMap<Id, User> idUsers = new HashMap<Id, User>();
		List<User> tmpUsers = userLoader.loadByCourseId(ctx.getCourseId());
		for (User user : tmpUsers)
		{
			// Map username to User object
			users.put(user.getUserName(), user);
			// Map user id to User object
			idUsers.put(user.getId(), user);
		}

		// Map username to course membership
		List<CourseMembership> tmpMemberships = courseMembershipLoader.loadByCourseId(ctx.getCourseId());
		for (CourseMembership member : tmpMemberships)
		{
			User user = idUsers.get(member.getUserId());
			courseMemberships.put(user.getId(), member);
		}

		// Map group name to Group object
		List<Group> tmpGroups = groupLoader.loadByCourseId(ctx.getCourseId());
		for (Group group : tmpGroups)
		{
			groups.put(group.getTitle(), group);
		}

		// store group memberships for lookup using first coursemembership and then group id
		List<GroupMembership> tmpGroupMemberships = groupMembershipLoader.loadByCourseId(ctx.getCourseId());
		for (GroupMembership member : tmpGroupMemberships)
		{
			Id courseMemberId = member.getCourseMembershipId();
			HashMap<Id, GroupMembership> tmpGMs;
			if (groupMemberships.containsKey(courseMemberId))
			{
				tmpGMs = groupMemberships.get(courseMemberId);
			}
			else
			{
				tmpGMs = new HashMap<Id, GroupMembership>();
				groupMemberships.put(courseMemberId, tmpGMs);
			}
			tmpGMs.put(member.getGroupId(), member);
		}
	}

	@Override
	public void setGroupSets(Map<String, GroupSet> importGroupSets) throws Exception {
		if (importGroupSets == null) {
			error("Group is empty!");
			throw new Exception("Group is empty!");
		}

		Context ctx = BlackboardUtil.extractContext(request);

		log("Initializing loaders...");
		initLoaders(ctx);

		log("Loading group sets from course " + ctx.getCourseId());
		List<Group> bbGroupSets = blackboard.persist.course.impl.GroupDAO.get()
				.loadGroupSetsOnly(ctx.getCourseId());

		// process the user entered groupsets
		for (Entry<String, GroupSet> entry : importGroupSets.entrySet()) {
			GroupSet set = entry.getValue();
			Group bbGroupSet = new Group();
			bbGroupSet.setId(null);
			bbGroupSet.setGroupSet(true);
			bbGroupSet.setTitle(set.getName());
			bbGroupSet.setDescription(new FormattedText());
			bbGroupSet.setCourseId(ctx.getCourseId());

			log("Processing group set: " + set.getName());

			// check to see if we need to create a new groupset
			if (!set.getName().equals(GroupSet.EMPTY_NAME)) {
				// find if the group set exists
				for (Group s : bbGroupSets) {
					if (s.getTitle().equals(set.getName())) {
						bbGroupSet = s;
					}
				}

				// if not, create it
				if (bbGroupSet.getId() == null) {
					groupPersister.persist(bbGroupSet);
				}
			}

			// check each group in this groupset
			for (Entry<String, ca.ubc.ctlt.group.GroGroup> entry1 : set
					.getGroups().entrySet()) {
				ca.ubc.ctlt.group.GroGroup group = entry1.getValue();

				// The 'add' operation means the user wants to add users to an existing group.
				// We ignore the group name supplied by the provider and replace it with
				// the name the user gave to the consumer instead. This is for GroupCreator.
				String op = request.getParameter("blackboardConsumerOperation");
				log("Group operation: " + op);
				if (op.equals("add")) {
					String groupId = request.getParameter("blackboardConsumerGroupSelection");
					Group tmp = groupLoader.loadById(Id.generateId(Group.DATA_TYPE, groupId));
					log("Adding to Group, Id: " + groupId + " Title: " + tmp.getTitle());
					group.setName(tmp.getTitle());
				}

				log("Processing group: " + group.getName());

				// creating or getting group from database
				Group bbGroup = groups.get(group.getName());
				if (bbGroup == null) {
					// group doesn't exist, create it
					try {
						bbGroup = createGroup(group, bbGroupSet);
					} catch (ValidationException e) {
						error("Failed to create group: "+group.getName()+". ("+e.getMessage()+")");
						continue;
					}
				}

				// tracking # of groups and last group used for completion message
				numGroups++;
				lastGroup = bbGroup;

				// creating members in the group
				createMembership(bbGroup, group.getMemberList());
			}
		}

		goodbye(ctx.getCourseId());
	}

	private Group createGroup(ca.ubc.ctlt.group.GroGroup group, Group groupSet) throws PersistenceException, ValidationException {
		GroupDbPersister groupDbPersister;

		groupDbPersister = (GroupDbPersister) bbPm.getPersister(GroupDbPersister.TYPE);

		// create the group
		Group bbGroup = new Group();
		bbGroup.setTitle(group.getName());
		// Need to provide an empty string for group description
		// The default desc is a null which causes BBL to throw an exception with the new group tool
		FormattedText text = new FormattedText();
		bbGroup.setDescription(text);
		bbGroup.setCourseId(groupSet.getCourseId());
		bbGroup.setSetId(groupSet.getId());
		groupDbPersister.persist(bbGroup);

		return bbGroup;
	}

	private void createMembership(Group bbGroup, HashMap<String, ca.ubc.ctlt.group.GroUser> memberList) {

		// create membership
		for (Entry<String, ca.ubc.ctlt.group.GroUser> entry : memberList.entrySet()) {
			ca.ubc.ctlt.group.GroUser user = entry.getValue();
			User bbUser = null;

			try {
				bbUser = users.get(user.getUserName());
				CourseMembership courseMembership = courseMemberships.get(bbUser.getId());

				log("Creating membership for group " + bbGroup.getTitle());
				if (groupMemberships.containsKey(courseMembership.getId()) &&
					groupMemberships.get(courseMembership.getId()).containsKey(bbGroup.getId()))
				{ // user is already in the group, do nothing
					log("User " + bbUser.getUserName() + " already in group " + bbGroup.getTitle());
				}
				else
				{ // user isn't in the group, enrol
					GroupMembership groupMembership = new GroupMembership();
					groupMembership.setCourseMembershipId(courseMembership.getId());
					groupMembership.setGroupId(bbGroup.getId());
					groupMembershipPersister.persist(groupMembership);
				}
			} catch (PersistenceException e) {
				error("Failed to save membership " + bbUser.getUserName()
						+ " in group " + bbGroup.getTitle() + "( "
						+ e.getMessage() + ")");
			} catch (ValidationException e)
			{
				error("Inconsistent membership state. Failed to save user "
					+ bbUser.getUserName() + " in group "
					+ bbGroup.getTitle() + "( " + e.getMessage() + ")");
			}
		}
	}

	@Override
	public String getOptionsPage()
	{
		return "consumers/blackboard/blackboardconsumer.jsp";
	}

	@Override
	public String getName()
	{
		return "Blackboard";
	}

	@Override
	public String getDescription()
	{
		return "Create new, or add to exisiting, groups in Blackboard Learn.";
	}
}
