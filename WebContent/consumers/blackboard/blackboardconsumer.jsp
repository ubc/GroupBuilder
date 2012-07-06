<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"
  import="ca.ubc.ctlt.group.*, ca.ubc.ctlt.group.blackboard.*, java.util.List, java.util.ArrayList, blackboard.data.course.Group" %>
<%@ taglib prefix="bbNG" uri="/bbNG"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:includedPage ctxId="ctx">

<% if(false == ((Consumer)pageContext.getAttribute("consumer", pageContext.REQUEST_SCOPE)).getManager().getProvider().canProvideMultipleGroups()) {%>

<bbNG:jspBlock>
<%
String log = "";
BlackboardUtil bbutil = new BlackboardUtil(ctx);
ArrayList<GroupWrapper> wrappers = new ArrayList<GroupWrapper>();
List<Group> groups = bbutil.getAllBbGroups(ctx.getCourseId());
for (Group group : groups)
{
	wrappers.add(new GroupWrapper(group));
}
pageContext.setAttribute("groups", wrappers);
pageContext.setAttribute("log", log);
%>
</bbNG:jspBlock>
<pre>
${log}
</pre>


<ol>
	<li>
	<bbNG:selectElement name="blackboardConsumerOperation" isRequired="true" onchange="toggleExistingGroups();">
		<bbNG:selectOptionElement value="create" optionLabel="Create New Group" />
		<bbNG:selectOptionElement value="add" optionLabel="Add to Existing Group" />
	</bbNG:selectElement>
	</li>

	<li id="existingGroups" style="display:none;">
	<bbNG:selectElement name="blackboardConsumerGroupSelection" isRequired="true">
		<c:forEach var="group" items="${groups}">
			<bbNG:selectOptionElement value="${group.idStr}" optionLabel="${group.title}" />
		</c:forEach>
	</bbNG:selectElement>
	</li>
</ol>

<bbNG:jsBlock>
<script type="text/javascript">
function toggleExistingGroups()
{
	var select = $('blackboardConsumerOperation');
	if (select[select.selectedIndex].value == "add")
	{
		$('existingGroups').show();
	}
	else
	{
		$('existingGroups').hide();
	}
}
</script>
</bbNG:jsBlock>

<% } %>

</bbNG:includedPage>