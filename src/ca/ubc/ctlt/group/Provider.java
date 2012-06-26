package ca.ubc.ctlt.group;

import java.util.HashMap;

public abstract class Provider extends ManagedObject
{

	public abstract HashMap<String, GroupSet> getGroupSets() throws Exception;

	/**
	 * Unfortunately, if there is a file upload element, we need to modify the
	 * html form element
	 * to have a multipart/form-data encoding. If this function returns true,
	 * then this provider
	 * has a file upload element and needs special encoding.
	 * 
	 * @return
	 */
	public boolean hasFileUpload()
	{
		return false;
	}
}
