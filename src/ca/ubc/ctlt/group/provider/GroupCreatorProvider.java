package ca.ubc.ctlt.group.provider;

import java.util.HashMap;

import ca.ubc.ctlt.group.Group;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.GroUser;

public class GroupCreatorProvider extends Provider
{

	@Override
	public HashMap<String, GroupSet> getGroupSets() throws Exception
	{
		HashMap<String, GroupSet> ret = new HashMap<String, GroupSet>();
		// use the default groupset since we don't support creating groupsets yet
		GroupSet set = new GroupSet(GroupSet.EMPTY_NAME);
		
		// get the parameters passed from the form
		String groupName = request.getParameter("name");
		String[] users = request.getParameterValues("usersSelected");
		
		// create the group
		Group group = new Group(groupName);
		
		// populate the group
		for (String user : users)
		{
			group.addMember(new GroUser(user));
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
		return "Create a group based on grade center fields.";
	}

}
