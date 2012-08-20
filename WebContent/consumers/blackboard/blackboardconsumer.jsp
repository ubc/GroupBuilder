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
BlackboardUtil bbutil = new BlackboardUtil(ctx);
ArrayList<GroupWrapper> wrappers = new ArrayList<GroupWrapper>();
List<Group> groups = bbutil.getAllBbGroups(ctx.getCourseId());
for (Group group : groups)
{
	wrappers.add(new GroupWrapper(group));
}
pageContext.setAttribute("groups", wrappers);
%>
</bbNG:jspBlock>

<div class="ie8hacks">
<ol>
	<li>
	<bbNG:selectElement name="blackboardConsumerOperation" isRequired="true" onchange="toggleExistingGroups(); return false;">
		<bbNG:selectOptionElement value="create" optionLabel="Create New Group" />
		<bbNG:selectOptionElement value="add" optionLabel="Add to Existing Group" />
	</bbNG:selectElement>
	</li>

	<li id="bbConsumerExistingGroups" style="display:none;">
	<h3>Select Group</h3>
	<bbNG:selectElement name="blackboardConsumerGroupSelection" isRequired="true" onchange="bbConsumerShowUsersList(); return false;">
		<c:forEach var="group" items="${groups}">
			<bbNG:selectOptionElement value="${group.idStr}" optionLabel="${group.title}" />
		</c:forEach>
	</bbNG:selectElement>
	</li>
	<li id="bbConsumerGroup">
		<span id="bbConsumerViewStatus"></span>
		<h3>Existing Users in Group</h3>
		<div id="bbConsumerGroupView">
		</div>
	</li>
</ol>
</div>

<bbNG:jsBlock>
<script type="text/javascript">
// hide the group viewer on page load 
$('bbConsumerGroup').hide();

function toggleExistingGroups()
{
	var select = $('blackboardConsumerOperation');
	if (select[select.selectedIndex].value == "add")
	{
		$('bbConsumerExistingGroups').show();
		$('bbConsumerGroup').show();
		bbConsumerShowUsersList(); // need to init the group users display 
	}
	else
	{
		$('bbConsumerExistingGroups').hide();
		$('bbConsumerGroup').hide();
	}
}

function bbConsumerShowUsersList()
{
	// need to add course_id for BBL to know where it is 
	new Ajax.Updater('bbConsumerGroupView',
			'consumers/blackboard/groupview.jsp',
			{
				parameters : {
					'blackboardConsumerOperation': $F('blackboardConsumerOperation'), 
					'blackboardConsumerGroupSelection' : $F('blackboardConsumerGroupSelection')
				},
				// enable js evaluation of the response or the 'select all' checkbox for inventoryList won't work 
				evalScripts : true,
				onCreate : function () { $('bbConsumerViewStatus').update("Searching, please wait..."); },
				onComplete : function () 
				{ 
					$('bbConsumerViewStatus').update(); 
				}
			});
}

</script>
</bbNG:jsBlock>

<% } %>

</bbNG:includedPage>