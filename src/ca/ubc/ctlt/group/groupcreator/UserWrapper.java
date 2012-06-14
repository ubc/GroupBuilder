package ca.ubc.ctlt.group.groupcreator;

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
	private String searchFieldName;
	private String searchFieldValue;
	
	public UserWrapper(CourseMembership member, String fieldName, String fieldValue)
	{
		this(member);
		searchFieldName = fieldName;
		searchFieldValue = fieldValue;
	}
	
	/**
	 * @param member
	 * @param user
	 */
	public UserWrapper(CourseMembership member)
	{
		this.member = member;
		this.user = member.getUser();
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

	/**
	 * @return the searchFieldName
	 */
	public String getSearchFieldName()
	{
		return searchFieldName;
	}

	/**
	 * @return the searchFieldValue
	 */
	public String getSearchFieldValue()
	{
		return searchFieldValue;
	}

	
}
