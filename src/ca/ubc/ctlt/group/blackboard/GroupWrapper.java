package ca.ubc.ctlt.group.blackboard;

import blackboard.data.course.Group;

public class GroupWrapper
{
	private Group group;
	
	public GroupWrapper(Group group) 
	{
		this.group = group;
	}
	
	public String getIdStr()
	{
		return group.getId().getExternalString();
	}
	
	public String getTitle()
	{
		return group.getTitle();
	}

}
