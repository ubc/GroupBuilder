<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@ taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:learningSystemPage ctxId="ctx" entitlement="course.groups.CREATE">
	
	<bbNG:pageHeader instructions="Please Select Operation">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroupBuilder"/>
	</bbNG:pageHeader>
	
<style type="text/css">
#shortcuts
{
	margin-bottom: 7em;
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
#tinyfootnote
{
	margin-top: 5em;
	font-size: 0.7em;
	text-align: center;
	color: #aaa;
}
#tinyfootnote a:link, #tinyfootnote a:visited
{
	color: #888;
}
#advancedOptionsToggle
{
	font-size: 0.9em;
}
p
{
	margin: 0.5em;
}
</style>
<!--[if (lt IE 9)]>
<style type="text/css">
/* IE8 CSS hacks */
select, .ie8hacks
{
	margin-left: 3em;
}
select
{
	margin-bottom: 3em;
}
</style>
<![endif]-->

<h1>Select An Option</h1>
<ul id="shortcuts">
<li><a href='#' onclick="shortcut('group');">GroupCreator</a> - Specify group enrolment by filtering students based on grade center fields.</li>
<li><a href='#' onclick="shortcut('import');">CSV Import</a> - Create groups from a CSV file.</li>
<li><a href='#' onclick="shortcut('export');">CSV Export</a> - Export existing groups into a CSV file.</li>
<li>
	<a href="http://wiki.ubc.ca/Documentation:Connect/Use_the_GroupBuilder_Tool" target="_blank">
	View tutorial style documentation on the UBC Wiki
	</a>
</li>
</ul>
	
<%
Manager manager =  new Manager();

ArrayList<Provider> providers = new ArrayList<Provider>();
ArrayList<Consumer> consumers = new ArrayList<Consumer>();

for (Class<Provider> p : manager.getProviders())
{
	providers.add(p.newInstance());
}

for (Class<Consumer> c : manager.getConsumers())
{
	consumers.add(c.newInstance());
}

pageContext.setAttribute("providers", providers);
pageContext.setAttribute("consumers", consumers);

%>

<p id="advancedOptionsToggle"><a href="#" onclick="$('advancedOptions').toggle(); return false;">Advanced Configuration</a></p>
<div id="advancedOptions">
<h1>Advanced Configuration</h1>
<p>
Please select a handler for Group Source and Group Destination. The Group Source handler reads in a list of groups 
from some input, 
which is then passed to the Group Destination handler. The destination handler will then write the list of groups 
to some form of 
output.
</p>
<p>E.g.: The Blackboard provider provides a list of existing groups stored in Blackboard Learn.</p>
<p>E.g.: The CSV consumer takes the list of groups and writes it out as a CSV file to be downloaded.</p>
<bbNG:form action="options.jsp" method="post">
<bbNG:dataCollection>
	<bbNG:step title="Group Source - Where group information is read or specified">
		<select name="provider" id="providersList" onchange="toggleDescriptions();">
			<c:forEach var="p" items="${providers}">
				<option value="${p.class.name}">${p.name}</option>
			</c:forEach>
		</select>
		<div id="providerDescriptions">
			<c:forEach var="p" items="${providers}">
				<p id="${p.class.name}_description">${p.description}</p>
			</c:forEach>
		</div>
	</bbNG:step>
	<bbNG:step title="Group Destination - Process the group information from the previous step">
		<select name="consumer" id="consumersList" onchange="toggleDescriptions();">
			<c:forEach var="c" items="${consumers}">
				<option value="${c.class.name}">${c.name}</option>
			</c:forEach>
		</select>
		<div id="consumerDescriptions">
			<c:forEach var="c" items="${consumers}">
				<p id="${c.class.name}_description">${c.description}</p>
			</c:forEach>
		</div>
	</bbNG:step>
	<input type="hidden" name="course_id" value=<%=request.getParameter("course_id")%> />
	<bbNG:stepSubmit/>
</bbNG:dataCollection>
</bbNG:form>
</div>

<!-- Shortcut javascript -->
<bbNG:jsBlock>
<script type="text/javascript">
// hide the advanced options initially 
$('advancedOptions').toggle();
toggleDescriptions();

// Helper function for toggleDescriptions(), toggleDescriptions() loop through each
// description hiding or showing the proper one. This function is the one that processes
// each description by hiding the one that doesn't match the selected. 
// 'this' stores the value of the selected provider or consumer list 
function showDescription(desc)
{
	if (this + "_description" == desc.id)
	{
		desc.show();
	}
	else
	{
		desc.hide();
	}
}

// In advanced configuration, each provider and consumer has a description blurb that 
// needs to be shown when it is selected. 
function toggleDescriptions()
{
	$('providerDescriptions').childElements().each(showDescription, $F('providersList'));
	$('consumerDescriptions').childElements().each(showDescription, $F('consumersList'));
}

// Helper function for shortcut(), select an option in a select element based
// on the option's value attribute.
// 
// selectId - the id of the select element that we're searching in 
// optionVal - the value of the option to be selected 
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

<p id="tinyfootnote">Open source project, available on <a href="https://github.com/ubc/GroupBuilder" target="_blank">Github</a>.<br/> Special thanks to Bob Walker.</p>

</bbNG:learningSystemPage>
