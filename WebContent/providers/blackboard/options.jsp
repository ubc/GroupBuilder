<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.blackboard.BlackboardUtil, ca.ubc.ctlt.group.*, java.util.HashMap, java.util.Map"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage>

	<style type="text/css">
	#groupslist ul, #groupslist ol
	{
		margin-left: 1em;
		list-style-position: inside;
	}
	
	#groupslist li
	{
		list-style-type: square;
	}	
	
	#groupslist li li
	{
		list-style-type: disc;
	}
	</style>

	<div id="groupslist">
	<bbNG:jspBlock>
	<%
	HashMap<String, GroupSet> sets = null;
	BlackboardUtil util = new BlackboardUtil(bbContext);
	
		
	try 
	{
		sets = util.getGroupSets();
	} 
	catch (Exception e) 
	{
		out.println("Loading group from Blackboard failed! " + e.getMessage());
		e.printStackTrace();
	}
	
	if (sets.isEmpty()) {
		out.println("No Group info");
	}
	else
	{
		for (Map.Entry<String, GroupSet> setEntry : sets.entrySet())
		{
			out.println("<h3>" + setEntry.getKey() + "</h3>");
			out.println("<ul>");
			for (Map.Entry<String, Group> groupEntry : setEntry.getValue().getGroups().entrySet())
			{
				out.println("<li>");
				out.println("<h4>" + groupEntry.getKey() + "</h4>");
				out.println("<ol>");
				for (Map.Entry<String, GroUser> userEntry : groupEntry.getValue().getMemberList().entrySet())	
				{
					out.println("<li>" + userEntry.getValue().getStudentID() + "</li>");
				}
				out.println("</ol>");
				out.println("</li>");
			}
			out.println("</ul>");
		}
	}
	%>
	</bbNG:jspBlock>
	</div>
</bbNG:includedPage>

