<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="
	ca.ubc.ctlt.group.blackboard.*,
	blackboard.data.user.User,
	java.util.List
	"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:includedPage>

	<bbNG:jspBlock>
	<%
	String groupId = request.getParameter("blackboardConsumerGroupSelection");
	BlackboardUtil util = new BlackboardUtil(bbContext);
	List<User> usersList = util.getUsers(groupId);
	
	pageContext.setAttribute("usersList", usersList);
	%>
	</bbNG:jspBlock>
	
	<bbNG:inventoryList collection="${usersList}" objectVar="user"
		className="User" description="List of users in this course."
		initialSortCol="studentId" renderAjax="true" showAll="true">
		<bbNG:listElement label="Student ID" name="studentId" isRowHeader="true">
             ${user.studentId}
        </bbNG:listElement>
		<bbNG:listElement label="Last Name" name="familyName" >
             ${user.familyName}
        </bbNG:listElement>
		<bbNG:listElement label="First Name" name="givenName" >
             ${user.givenName}
        </bbNG:listElement>
	</bbNG:inventoryList>
	
</bbNG:includedPage>

