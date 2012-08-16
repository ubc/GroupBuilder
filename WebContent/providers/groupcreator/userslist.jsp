<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.groupcreator.*, java.util.ArrayList"%>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:includedPage>

	<bbNG:jspBlock>
	<%
		String[] fields = request.getParameterValues("searchField");
		String[] ops = request.getParameterValues("searchOp");
		String[] terms = request.getParameterValues("searchTerm");
		String combinationOp = request.getParameter("combinationOp");
		ArrayList<SearchCriteria> criterias = new ArrayList<SearchCriteria>();
		
		// we're using session to store previous search results in order for sorting to work
		// this parameter tells us to clear the session
		String clearSearch = request.getParameter("clearSearch");
		if (clearSearch != null)
		{
			session.removeAttribute("GroupCreatorProviderCriterias");	
		}
		// check to see if we need to convert the search parameters into search criterias
		if (fields != null && ops != null && terms != null && combinationOp != null) 
		{
			// and since field, term, and ops should have 1:1:1 correspondence, their arrays must be of the
			// same length
			if (fields.length != ops.length || fields.length != terms.length)
			{
				throw new IllegalArgumentException("Search criterias malformed.");
			}
		
			for (int i = 0; i < fields.length; i++)
			{
				criterias.add(new SearchCriteria(fields[i], ops[i], terms[i]));
			}
			// store the search criterias in the session
			session.setAttribute("GroupCreatorProviderCriterias", criterias);
		}
		else
		{ // try to restore the previous search criterias from the session 
			criterias = (ArrayList<SearchCriteria>) session.getAttribute("GroupCreatorProviderCriterias");
			if (criterias == null)
			{
				criterias = new ArrayList<SearchCriteria>();
			}
		}
		
		CourseUtil cc = new CourseUtil(bbContext);
		if (criterias.isEmpty())
		{
			// this is how you supply an appropriate Collection to inventoryList
			pageContext.setAttribute("usersList", cc.getUsers());
		}
		else
		{
			pageContext.setAttribute("usersList", cc.search(criterias, combinationOp));
		}
		//pageContext.setAttribute("debug", cc.getDebug()); // for debug
	%>
	</bbNG:jspBlock>
	
	<bbNG:inventoryList collection="${usersList}" objectVar="user"
		className="UserWrapper" description="List of users in this course."
		initialSortCol="familyName" renderAjax="true" showAll="true">
		<bbNG:listCheckboxElement name="usersSelected" value="${fn:escapeXml(user.userName)}" />
		<bbNG:listElement label="Student ID" name="studentId" isRowHeader="true" comparator="${user.cmStudentId}">
             ${user.studentId}
        </bbNG:listElement>
		<bbNG:listElement label="Last Name" name="familyName" comparator="${user.cmFamilyName}">
             ${user.familyName}
        </bbNG:listElement>
		<bbNG:listElement label="First Name" name="givenName" comparator="${user.cmGivenName}">
             ${user.givenName}
        </bbNG:listElement>
		<c:forEach var="fields" items="${user.searchFields}">
			<bbNG:listElement label="${fields.name}" name="${fields.name}">
				${fields.value}
	        </bbNG:listElement>
		</c:forEach>
		<bbNG:listElement label="Role" name="roleName" comparator="${user.cmRole}">
             ${user.role}
        </bbNG:listElement>
	</bbNG:inventoryList>
	
</bbNG:includedPage>

