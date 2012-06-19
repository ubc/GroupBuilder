package ca.ubc.ctlt.group;

import java.util.HashMap;

public abstract class Consumer extends ManagedObject {
	
	/**
	 * Process the given groupsets.
	 * 
	 * @param sets
	 * @throws Exception
	 */
	public abstract void setGroupSets(HashMap<String, GroupSet> sets) throws Exception;
}
