<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:learningSystemPage ctxId="ctx">
	
	<bbNG:pageHeader instructions="Please follow the steps">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroUP"/>
	</bbNG:pageHeader>
<%
Manager manager = new Manager(request, response);
manager.process();
%>

Finished.

<%=manager.getLogs() %>
<br />
<%=manager.getErrors() %>

</bbNG:learningSystemPage>