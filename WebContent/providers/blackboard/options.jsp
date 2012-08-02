<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.blackboard.BlackboardUtil, ca.ubc.ctlt.group.*, java.util.HashMap, java.util.Map"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage>

	<style type="text/css">
	#groupslist h3
	{
		background: #bef;
		padding: 0.5em;
		margin: 0em;
	}
	
	#groupslist ol
	{
		padding: 0.5em;
		margin: 0em;
		margin-bottom: 1em;
		list-style-position: inside;
		background: #eff;
	}
	
	#groupslist ol li
	{
		list-style-type: square;
		padding: 0.5em;
		margin: 0em;
	}	
	
	.bbProviderHideCss
	{
		display: none;
	}
	
	#groupslist p
	{
		margin-bottom: 1em;
	}
	</style>

	<div id="groupslist" class="ie8hacks">
	<p>List of groups, click to show group members list. Group members are listed in the format "Name (Student Number)".</p>
	<bbNG:jspBlock>
	<%
	Map<String, GroupSet> sets = null;
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
		int i = 0;
		for (Map.Entry<String, GroupSet> setEntry : sets.entrySet())
		{
			for (Map.Entry<String, Group> groupEntry : setEntry.getValue().getGroups().entrySet())
			{
				String olId = "BbProviderGroup" + i;
				out.println("<h3><a href='#' onclick='bbProviderToggleGroups("+ olId +"); return false;'>" + 
					groupEntry.getKey() + "</a></h3>");
				out.println("<ol id='"+ olId +"' class='bbProviderHideCss'>");
				for (Map.Entry<String, GroUser> userEntry : groupEntry.getValue().getMemberList().entrySet())	
				{
					out.println("<li>" + userEntry.getValue().getName() + " ("
						+ userEntry.getValue().getStudentID() + ")</li>");
				}
				out.println("</ol>");
				i++;
			}
		}
	}
	%>
	</bbNG:jspBlock>
	</div>
	
	<bbNG:jsBlock>
	<script type="text/javascript">
	function bbProviderToggleGroups(olId)
	{
		$(olId).toggleClassName("bbProviderHideCss");
	}
	</script>
	</bbNG:jsBlock>
	
</bbNG:includedPage>

