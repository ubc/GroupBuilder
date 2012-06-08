package ca.ubc.ctlt.group;

import java.util.HashMap;

public abstract class Provider extends ManagedObject{

	public abstract HashMap<String, GroupSet> getGroupSets() throws Exception;
}
