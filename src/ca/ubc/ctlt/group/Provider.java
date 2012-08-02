package ca.ubc.ctlt.group;

import java.util.Map;

import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public abstract class Provider extends ManagedObject {

	public abstract Map<String, GroupSet> getGroupSets(BlackboardUtil util);

	/**
	 * Unfortunately, if there is a file upload element, we need to modify the
	 * html form element to have a multipart/form-data encoding. If this
	 * function returns true, then this provider has a file upload element and
	 * needs special encoding.
	 * 
	 * @return
	 */
	public boolean hasFileUpload() {
		return false;
	}

	public boolean canProvideGroupSet() {
		return true;
	}

	public boolean canProvideMultipleGroups() {
		return true;
	}
}
