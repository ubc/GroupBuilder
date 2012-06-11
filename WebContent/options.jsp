<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>
<%@taglib prefix="bbUI" uri="/bbUI"%>
<%@taglib prefix="bbData" uri="/bbData"%>

<bbData:context id="ctx">
	<bbNG:learningSystemPage ctxId="ctx">
	
	<bbNG:pageHeader instructions="Please follow the steps">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroUP"/>
	</bbNG:pageHeader>
	
<%
Class p = Class.forName(request.getParameter("provider"));
Provider provider = (Provider)p.newInstance();
Class c = Class.forName(request.getParameter("consumer"));
Consumer consumer  = (Consumer)c.newInstance();
provider.setRequest(request);
provider.setResponse(response);
consumer.setRequest(request);
consumer.setResponse(response);
%>

<form action="process.jsp" method="post" ENCTYPE="multipart/form-data">

<%=p.getField("NAME").get(null)%> Provider Options <br />
<%=p.getField("DESCRIPTION").get(null) %>
<hr>

<%=provider.renderOptions()%>

<br /> <br />

<%=c.getField("NAME").get(null)%> Consumer Options <br />
<%=c.getField("DESCRIPTION").get(null) %>
<hr>

<%=consumer.renderOptions()%>

<input type="submit" value="Submit" />

<input type="hidden" name="provider" value="<%=p.getName()%>" />
<input type="hidden" name="consumer" value="<%=c.getName()%>" />
<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />

</form>

	</bbNG:learningSystemPage>

</bbData:context>