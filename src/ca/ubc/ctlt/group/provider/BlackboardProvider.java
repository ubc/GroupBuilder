package ca.ubc.ctlt.group.provider;

import java.util.Map;

import blackboard.persist.PersistenceException;

import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class BlackboardProvider extends Provider {

	@Override
	public Map<String, GroupSet> getGroupSets(BlackboardUtil util) {

		Map<String, GroupSet> sets = null;
		
		try {
			sets = util.getGroupSets();
		} catch (PersistenceException e) {
			log("Database error!");
		}
		
		return sets;
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
