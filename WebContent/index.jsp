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
Manager manager =  new Manager();
Class<Provider>[] providers = manager.getProviders();
Class<Consumer>[] consumers = manager.getConsumers();

int i;
 %>

<bbNG:form action="options.jsp" method="post">
<bbNG:dataCollection>
	<bbNG:step title="Provider">
		<select name="provider">
			<% for (i=0; i < providers.length; i++) { %>
				<option value="<%=providers[i].getName()%>"><%=providers[i].newInstance().getName()%></option>
			<% } %>
		</select>
	</bbNG:step>

	<bbNG:step title="Consumer">
		<select name="consumer">
			<% for (i=0; i < consumers.length; i++) { %>
				<option value="<%=consumers[i].getName()%>"><%=consumers[i].newInstance().getName()%></option>
			<% } %>
		</select>
	</bbNG:step>
	<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />
	<bbNG:stepSubmit/>
</bbNG:dataCollection>
</bbNG:form>

</bbNG:learningSystemPage>
