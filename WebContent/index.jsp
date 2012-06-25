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
	
<style type="text/css">
#shortcuts
{
	margin-bottom: 3em;
}
#shortcuts li
{
	padding: 1em;
	font-size: 1.5em;
}
#shortcuts li a
{
	font-weight: bold;
}
</style>

<h1>Shortcuts</h1>
<ul id="shortcuts">
<li><a href='#' onclick="shortcut('group');">GroupCreator</a> - Create groups based on grade center columns.</li>
<li><a href='#' onclick="shortcut('import');">CSV Import</a> - Create groups from a CSV file.</li>
<li><a href='#' onclick="shortcut('export');">CSV Export</a> - Export existing groups into a CSV file.</li>
</ul>
	
<%
Manager manager =  new Manager();
Class<Provider>[] providers = manager.getProviders();
Class<Consumer>[] consumers = manager.getConsumers();

int i;
 %>

<h1>Manual Configuration</h1>
<bbNG:form action="options.jsp" method="post">
<bbNG:dataCollection>
	<bbNG:step title="Provider">
		<select name="provider" id="providersList" >
			<% for (i=0; i < providers.length; i++) { %>
				<option value="<%=providers[i].getName()%>"><%=providers[i].newInstance().getName()%></option>
			<% } %>
		</select>
	</bbNG:step>

	<bbNG:step title="Consumer">
		<select name="consumer" id="consumersList" >
			<% for (i=0; i < consumers.length; i++) { %>
				<option value="<%=consumers[i].getName()%>"><%=consumers[i].newInstance().getName()%></option>
			<% } %>
		</select>
	</bbNG:step>
	<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />
	<bbNG:stepSubmit/>
</bbNG:dataCollection>
</bbNG:form>

<!-- Shortcut javascript -->
<bbNG:jsBlock>
<script type="text/javascript">
function selectOption(selectId, optionVal)
{
	var options = $$('select#' + selectId + ' option');
	var len = options.length;
	for (var i = 0; i < len; i++) 
	{
		if (options[i].value == optionVal)
		{
			options[i].selected = true;
		}
	}
}

function shortcut(type)
{
	var provider = "";
	var consumer = "";
	if (type == 'group')
	{
		provider = "ca.ubc.ctlt.group.provider.GroupCreatorProvider";
		consumer = "ca.ubc.ctlt.group.consumer.BlackboardConsumer";
	}
	else if (type == 'import')
	{
		provider = "ca.ubc.ctlt.group.provider.CsvProvider";
		consumer = "ca.ubc.ctlt.group.consumer.BlackboardConsumer";
	}
	else if (type == 'export')
	{
		provider = "ca.ubc.ctlt.group.provider.BlackboardProvider";
		consumer = "ca.ubc.ctlt.group.consumer.CsvConsumer";
	}
	selectOption('providersList', provider);
	selectOption('consumersList', consumer);
	document.forms[0].submit();
}
</script>
</bbNG:jsBlock>

</bbNG:learningSystemPage>
