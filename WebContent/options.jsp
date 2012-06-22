<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>

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
	pageContext.setAttribute("provider", provider);
	pageContext.setAttribute("consumer", consumer);
	%>

	<bbNG:form action="process.jsp" method="post" onSubmit="return validateForm();">
		<bbNG:dataCollection>
		
			<bbNG:step title="${provider.name} Provider - ${provider.description}">
				<jsp:include page="${provider.optionsPage}" flush="true" />
			</bbNG:step>
			
			<bbNG:step title="${consumer.name} Provider - ${consumer.description}">
				<jsp:include page="${consumer.optionsPage}" flush="true" />
			</bbNG:step>
			
			<input type="hidden" name="provider" value="${provider.class.name}" />
			<input type="hidden" name="consumer" value="${consumer.class.name}" />
			<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />
			
			<bbNG:stepSubmit/>
		
		</bbNG:dataCollection>
	</bbNG:form>
	
</bbNG:learningSystemPage>