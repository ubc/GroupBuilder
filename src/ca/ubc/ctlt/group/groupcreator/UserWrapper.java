package ca.ubc.ctlt.group.groupcreator;

import java.util.LinkedHashSet;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;

/**
 * Stupid Java Bean wrapper to make it easier to access the fields 
 * I need for displaying users using bbNG:inventoryList
 * 
 * @author john
 *
 */
public class UserWrapper
{
	private CourseMembership member;
	private User user;
	private LinkedHashSet<SearchFieldInfo> searchFields = new LinkedHashSet<SearchFieldInfo>();
	
	/**
	 * @param member
	 */
	public UserWrapper(CourseMembership member)
	{
		this.member = member;
		this.user = member.getUser();
	}
	
	/**
	 * @return the member
	 */
	public CourseMembership getMember()
	{
		return member;
	}

	public void addSearchFields(String name, String value)
	{
		searchFields.add(new SearchFieldInfo(name, value));
	}
	
	public LinkedHashSet<SearchFieldInfo> getSearchFields()
	{
		return searchFields;
	}
	
	public String getUserName()
	{
		return user.getUserName();
	}
	
	public String getGivenName()
	{
		return user.getGivenName();
	}
	
	public String getFamilyName()
	{
		return user.getFamilyName();
	}
	
	public String getStudentId()
	{
		return user.getStudentId();
	}
	
	public String getRole()
	{
		return member.getRoleAsString();
	}
	
	public String getIdString()
	{
		return user.getId().getExternalString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		UserWrapper other = (UserWrapper) obj;
		if (user == null)
		{
			if (other.user != null)
				return false;
		}
		else if (!user.equals(other.user))
			return false;
		return true;
	}

}
