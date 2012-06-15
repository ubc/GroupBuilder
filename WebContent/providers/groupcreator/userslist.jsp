<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.groupcreator.*"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage>

	<bbNG:jspBlock>
	<%
		String sField = request.getParameter("searchField");
		String sOp = request.getParameter("searchOp");
		String sTerm = request.getParameter("searchTerm");
		
		CourseUtil cc = new CourseUtil(bbContext);
		if (sField == null || sOp == null || sTerm == null)
		{
			// this is how you supply an appropriate Collection to inventoryList
			pageContext.setAttribute("usersList", cc.getUsers());
		}
		else
		{
			pageContext.setAttribute("usersList", cc.search(sField, sOp, sTerm));
		}
	%>
	</bbNG:jspBlock>

	<bbNG:inventoryList collection="${usersList}" objectVar="user"
		className="UserWrapper" description="List of users in this course.">
		<bbNG:listCheckboxElement name="usersSelected" value="${user.userName}" />
		<bbNG:listElement label="User Name" name="userName" isRowHeader="true">
             ${user.userName}
        </bbNG:listElement>
		<bbNG:listElement label="${user.searchFieldName}" name="searchField">
			${user.searchFieldValue}
        </bbNG:listElement>
		<bbNG:listElement label="Family Name" name="familyName">
             ${user.familyName}
        </bbNG:listElement>
		<bbNG:listElement label="Given Name" name="givenName">
             ${user.givenName}
        </bbNG:listElement>
		<bbNG:listElement label="Student ID" name="studentId">
             ${user.studentId}
        </bbNG:listElement>
		<bbNG:listElement label="Role" name="roleName">
             ${user.role}
        </bbNG:listElement>
	</bbNG:inventoryList>
	
</bbNG:includedPage>

