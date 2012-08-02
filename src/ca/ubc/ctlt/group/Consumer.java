package ca.ubc.ctlt.group;

import java.util.Map;

public abstract class Consumer extends ManagedObject {
	
	/**
	 * Process the given groupsets.
	 * 
	 * @param sets
	 * @throws Exception
	 */
	public abstract void setGroupSets(Map<String, GroupSet> sets) throws Exception;
}
