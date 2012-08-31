package ca.ubc.ctlt.group.provider;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ctlt.group.GroUser;
import ca.ubc.ctlt.group.GroGroup;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class GroupCreatorProvider extends Provider
{
	@Override
	public Map<String, GroupSet> getGroupSets(BlackboardUtil util)
	{
		HashMap<String, GroupSet> ret = new HashMap<String, GroupSet>();
		// use the default groupset since we don't support creating groupsets yet
		GroupSet set = new GroupSet(GroupSet.EMPTY_NAME);
		
		// get the parameters passed from the form
		String groupName = request.getParameter("name");
		String[] users = request.getParameterValues("usersSelected");
		
		// create the group
		GroGroup group = new GroGroup(groupName);
		
		// populate the group
		if (users == null)
		{ // no users selected
			error("No users selected for group.");
			return null;
		}
		else
		{
			for (String user : users)
			{
				group.addMember(new GroUser(new BlackboardUtil(request).findUserByUsername(user)));
			}
		}
		
		// add group to set, and set to return
		set.addGroup(group);
		ret.put(GroupSet.EMPTY_NAME, set);
		
		return ret;
	}

	@Override
	public String getOptionsPage()
	{
		return "providers/groupcreator/groupcreator.jsp";
	}

	@Override
	public String getName()
	{
		return "GroupCreator";
	}

	@Override
	public String getDescription()
	{
		return "Find users based on grade center fields.";
	}

	@Override
	public boolean canProvideGroupSet() {
		return false;
	}

	@Override
	public boolean canProvideMultipleGroups() {
		return false;
	}
}
