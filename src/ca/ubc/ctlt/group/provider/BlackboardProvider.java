package ca.ubc.ctlt.group.provider;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
			log("Getting groups that are in groupsets.");
			Map<String, GroupSet> ret = new HashMap<String, GroupSet>();
			// Really terrible implementation, but time strapped
			// add groups that are in group sets
			for (Map.Entry<String, GroupSet> e : util.getGroupSets().entrySet()) {
				log("Processing Group Set: " + e.getKey());
				String groupSetId = e.getKey();
				String groupSetName = e.getValue().getName();
				GroupSet tmpSet = new GroupSet(groupSetName);
				tmpSet.setId(groupSetId);
				// check that the groups in this groupset are wanted
				for (Group group : keep) {
					log("Checking if group " + group.getTitle() + " is in this groupset.");
					Map<String, GroGroup> groups = e.getValue().getGroups();
					if (groups.containsKey(group.getTitle())) {
						log("Yes, " + group.getTitle() + " is in this groupset.");
						tmpSet.addGroup(groups.get(group.getTitle()));
						log("Added group object to tmp");
					}
				}
				log("Checking if tmp has values");
				// if we have any wanted groups in this groupset, add to return value
				if (!tmpSet.getGroups().isEmpty()) {
					log("Adding tmp to ret");
					ret.put(groupSetId, tmpSet);
				}
				log("Done with this groupset");
			}
			
			log("Getting groups that don't have groupsets.");
			// add groups that are not in group sets
			for (Group group : keep) {
				log("Checking group: " + group.getTitle());
				if (!group.isInGroupSet()) {
					log("Group isn't in groupset, added.");
					defaultSet.addGroup(new GroGroup(group));
				}
			}
			if (!defaultSet.getGroups().isEmpty()) {
				ret.put(defaultSet.getName(), defaultSet);
			}
			
			return ret;
		} catch (PersistenceException e)
		{
			log("Database error!");
		} catch (Exception e)
		{
			log("Unexpected exception: " + e.toString());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			String content = baos.toString();
			log("Unexpected exception trace: " + content);
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
