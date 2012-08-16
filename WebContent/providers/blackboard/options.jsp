<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="
	ca.ubc.ctlt.group.blackboard.BlackboardUtil, 
	blackboard.data.course.Group, 
	ca.ubc.ctlt.group.*, 
	java.util.List, 
	blackboard.base.GenericFieldComparator
	"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:includedPage>

	<bbNG:jspBlock>
	<%
	BlackboardUtil util = new BlackboardUtil(bbContext);
	List<Group> groups;
	
		
	try 
	{
		groups = util.getGroups();
		pageContext.setAttribute("groups", groups);
	} 
	catch (Exception e) 
	{
		out.println("Loading group from Blackboard failed! " + e.getMessage());
		e.printStackTrace();
	}
	
	pageContext.setAttribute("cmGroupTitle", new GenericFieldComparator<Group>("getTitle", Group.class));
	%>
	</bbNG:jspBlock>
	<div id="groupslist" class="ie8hacks">
	<h3>Select Groups to Export</h3>
	<bbNG:inventoryList collection="${groups}" objectVar="group"
		className="Group" description="List of groups in this course."
		initialSortCol="title" renderAjax="true" showAll="true">
		<bbNG:listCheckboxElement name="groupsSelected" value="${fn:escapeXml(group.title)}" />
		<bbNG:listElement label="Group Name" name="title" isRowHeader="true" comparator="${cmGroupTitle}">
             ${group.title}
        </bbNG:listElement>
	</bbNG:inventoryList>
	</div>
</bbNG:includedPage>

