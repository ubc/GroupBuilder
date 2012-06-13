<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.blackboard.BlackboardUtil, ca.ubc.ctlt.group.*, java.util.HashMap"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage>

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
		out.println(sets.toString());
	}
	%>
	</bbNG:jspBlock>

</bbNG:includedPage>

