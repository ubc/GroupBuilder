package ca.ubc.ctlt.group;

import java.util.HashMap;
import java.util.List;

public abstract class Consumer extends ManagedObject {
	
	public abstract void setGroupSets(HashMap<String, GroupSet> sets) throws Exception;
}
