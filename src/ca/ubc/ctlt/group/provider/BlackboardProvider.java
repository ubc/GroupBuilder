package ca.ubc.ctlt.group.provider;

import java.util.HashMap;

import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class BlackboardProvider extends Provider {
	
	@Override
	public HashMap<String, GroupSet> getGroupSets() throws Exception {

		HashMap<String, GroupSet> sets;
		
		BlackboardUtil util = new BlackboardUtil(request);
		sets = util.getGroupSets();
		
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
		return "Providing group information from Blackboard Learn.";
	}

}
