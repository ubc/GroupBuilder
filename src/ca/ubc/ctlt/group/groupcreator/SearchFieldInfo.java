package ca.ubc.ctlt.group.groupcreator;

/* It feels kinda ridiculous that you need to create classes just to
 * group some simple strings together.
 */
public class SearchFieldInfo
{
	private String name;
	private String value;
	/**
	 * @param name
	 * @param value
	 */
	public SearchFieldInfo(String name, String value)
	{
		super();
		this.name = name;
		this.value = value;
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchFieldInfo other = (SearchFieldInfo) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (value == null)
		{
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}
	

}
