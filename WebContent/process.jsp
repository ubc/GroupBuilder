<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:learningSystemPage ctxId="ctx">
	<bbNG:pageHeader instructions="Result">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroupBuilder"/>
	</bbNG:pageHeader>
	
<style>
.GroupManagerSuccess
{
	color: green;
}
.GroupManagerFail
{
	color: red;
}
.GroupManagerLogToggle
{
	margin-top: 5em;
}
.GroupManagerLogToggle, #GroupManagerLogs li
{
	font-size: 0.7em;
}
</style>
<!--[if (lt IE 9)]>
<style type="text/css">
/* IE8 CSS hacks */
.ie8hacks
{
	margin-left: 3em;
}
</style>
<![endif]-->
<%
Manager manager = new Manager(request, response);
manager.process();
pageContext.setAttribute("manager", manager);
pageContext.setAttribute("errors", manager.getErrors());
%>
		
<c:choose>
    <c:when test="${empty errors}">
		<h1 class="GroupManagerSuccess">Successful!</h1>
    </c:when>
    <c:otherwise>
		<h1 class="GroupManagerFail">Errors While Processing</h1>
		<ul id="GroupManagerErrors">
			<c:forEach var="error" items="${errors}">
				<li><c:out value="${error}"  /></li>
			</c:forEach>
		</ul>
    </c:otherwise>
</c:choose>

<div>
<h6 class="GroupManagerLogToggle"><a href="#" onclick="GroupManagerHideLogs(); return false;">Show/Hide Logs</a></h6>
<ul id="GroupManagerLogs">
	<c:forEach var="log" items="${manager.logs}">
		<li><c:out value="${log}"  /></li>
	</c:forEach>
</ul>
</div>

<bbNG:jsBlock>
<script type="text/javascript">
GroupManagerHideLogs();
function GroupManagerHideLogs() 
{
	$('GroupManagerLogs').toggle();
}
</script>
</bbNG:jsBlock>

</bbNG:learningSystemPage>