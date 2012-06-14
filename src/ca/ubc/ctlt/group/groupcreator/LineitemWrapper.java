package ca.ubc.ctlt.group.groupcreator;

import blackboard.data.gradebook.Lineitem;

public class LineitemWrapper implements Comparable<LineitemWrapper>
{
	private Lineitem item;
	
	public LineitemWrapper(Lineitem item)
	{
		this.item = item;
	}
	
	public String getName()
	{
		return item.getName();
	}
	
	public String getIdString()
	{
		return item.getId().getExternalString();
	}
	
	public Lineitem getItem()
	{
		return item;
	}

	@Override
	public int compareTo(LineitemWrapper arg0)
	{
		return getName().compareTo(arg0.getName());
	}
	
}
