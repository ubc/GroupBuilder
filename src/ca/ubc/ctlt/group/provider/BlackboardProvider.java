package ca.ubc.ctlt.group.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blackboard.data.course.Group;
import blackboard.persist.PersistenceException;

import ca.ubc.ctlt.group.GroGroup;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class BlackboardProvider extends Provider {

	@Override
	public Map<String, GroupSet> getGroupSets(BlackboardUtil util) {
		String[] groupNames = request.getParameterValues("groupsSelected");
		List<Group> keep = new ArrayList<Group>();
		
		try {
			// filter out groups selected
			List<Group> all = util.getGroups();
			for (String name : groupNames) {
				for (Group group : all) {
					log("Name: " + name + " Title: " + group.getTitle());
					if (group.getTitle().equals(name)) {
						keep.add(group);
					}
				}
			}
		} catch (PersistenceException e) {
			log("Database error!");
		}
		
		// Convert the groups selected to groupsets
		// first get all groupsets
		GroupSet defaultSet = new GroupSet(GroupSet.EMPTY_NAME);
		try {
			log("Here1");
			Map<String, GroupSet> ret = new HashMap<String, GroupSet>();
			// Really terrible implementation, but time strapped
			// add groups that are in group sets
			for (Map.Entry<String, GroupSet> e : util.getGroupSets().entrySet()) {
				GroupSet tmpSet = new GroupSet(e.getKey());
				// check that the groups in this groupset are wanted
				for (Group group : keep) {
					Map<String, GroGroup> groups = e.getValue().getGroups();
					if (groups.containsKey(group.getTitle())) {
						tmpSet.addGroup(groups.get(group.getTitle()));
					}
				}
				// if we have any wanted groups in this groupset, add to return value
				if (!tmpSet.getGroups().isEmpty()) {
					ret.put(tmpSet.getName(), tmpSet);
				}
			}
			log("Here2y");
			
			// add groups that are not in group sets
			for (Group group : keep) {
				log("Check: " + group.getTitle());
				if (!group.isInGroupSet()) {
					log("Added: " + group.getTitle());
					defaultSet.addGroup(new GroGroup(group));
				}
			}
			if (!defaultSet.getGroups().isEmpty()) {
				log("Is good!");
				ret.put(defaultSet.getName(), defaultSet);
			}
			
			return ret;
		} catch (PersistenceException e)
		{
			log("Database error!");
		}
		return null;
	}


	@Override
	public String getOptionsPage()
	{
		return "providers/blackboard/options.jsp";
	}


	@Override
	public String getName()
	{
		return "Blackboard";
	}


	@Override
	public String getDescription()
	{
		return "Read group information for this course from Blackboard Learn.";
	}

}
