<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:learningSystemPage ctxId="ctx" entitlement="course.groups.CREATE">
	
	<bbNG:pageHeader instructions="Please Follow the Steps">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroupBuilder"/>
	</bbNG:pageHeader>
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
/* 	Class p = Class.forName(request.getParameter("provider"));
	Provider provider = (Provider)p.newInstance();
	Class c = Class.forName(request.getParameter("consumer"));
	Consumer consumer  = (Consumer)c.newInstance();
	provider.setRequest(request);
	provider.setResponse(response);
	consumer.setRequest(request);
	consumer.setResponse(response); */
	pageContext.setAttribute("provider", manager.getProvider(), pageContext.REQUEST_SCOPE);
	pageContext.setAttribute("consumer", manager.getConsumer(), pageContext.REQUEST_SCOPE);
	pageContext.setAttribute("enctype", "application/x-www-form-urlencoded");
	if (manager.getProvider().hasFileUpload())
	{
		pageContext.setAttribute("enctype", "multipart/form-data");
	}
	%>

	<bbNG:form action="process.jsp" method="post" onSubmit="return validateForm();" enctype="${enctype}">
		<bbNG:dataCollection>
		
			<bbNG:step title="Group Source - ${provider.name}">
				<jsp:include page="${provider.optionsPage}" flush="true" />
			</bbNG:step>
			
			<bbNG:step title="Group Destination - ${consumer.name}">
				<jsp:include page="${consumer.optionsPage}" flush="true" />
			</bbNG:step>
			
			<input type="hidden" name="provider" value="${provider.class.name}" />
			<input type="hidden" name="consumer" value="${consumer.class.name}" />
			<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />
			
			<bbNG:stepSubmit/>
		
		</bbNG:dataCollection>
	</bbNG:form>
	
</bbNG:learningSystemPage>