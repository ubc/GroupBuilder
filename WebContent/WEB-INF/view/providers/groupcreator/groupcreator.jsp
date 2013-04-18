<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"
  import="ca.ubc.ctlt.group.groupcreator.*, java.util.Map" %>
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage ctxId="ctx">

<style type='text/css'>
#groupcreatorInstructions
{
	margin-top: 1em;
}
#groupcreatorInstructions li
{
	list-style:	decimal inside;
}
#gcNameWarning
{
	font-weight: bold;
	color: red;
	margin: 0.5em 0;
}
</style>

<div class="ie8hacks">
<ul>
	<li>
		<h4>Instructions</h4>
		<ol id='groupcreatorInstructions'>
			<li>Enter a name for the group.</li>
			<li>Enter search parameters and then click search.</li>
			<li>Check that the student selections are correct and click submit.</li>
		</ol>
	</li>
	<li id="gcGroupName">
		<div class="label">
			<label for="name">
			Name
			</label>
		</div>
		<bbNG:textElement name="name" isRequired="true" onkeypress="gcCheckName();" />
		<p id="gcNameWarning"></p>
	</li> 
</ul>
<h3 class="steptitle">Search</h3>
<ul>
<li>
	<bbNG:button label="Add Search Criteria" onClick="gcAddCriteria();return false;" />
</li>
<li>
	<!-- if the user adds additional criterias, we basically just duplicate the entire searchOptions div -->
	<div id="searchOptions">
		<div class="label">
			<label for="field">
			Grade Center Field
			</label>
		</div>
		<bbNG:jspBlock>
		<%
		GradeCenterUtil gc = new GradeCenterUtil(ctx);
		CourseUtil cc = new CourseUtil(ctx);
		out.print("<select name=\"searchField\" >");
		for (LineitemWrapper c : gc.getLineitemColumns())
		{
			out.print("<option value=" + c.getIdString() + ">" + c.getName() + "</option>");
		}
		for (Map.Entry<String, String> c : GradeCenterUtil.getUserinfoColumns().entrySet())
		{
			out.print("<option value=" + c.getKey() + ">" + c.getValue() + "</option>");
		}
		out.print("</select>");
		pageContext.setAttribute("gcGroupsList", cc.getGroupsJSON());	
		%>
		</bbNG:jspBlock>
		<bbNG:selectElement name="searchOp" isRequired="true">
			<bbNG:selectOptionElement value="contains" optionLabel="Contains" />
			<bbNG:selectOptionElement value="exactly" optionLabel="Exactly" />
			<bbNG:selectOptionElement value="exclude" optionLabel="Exclude" />
			<bbNG:selectOptionElement value="greater" optionLabel="Greater Than (numeric)" />
			<bbNG:selectOptionElement value="equal" optionLabel="Equal (numeric)" />
			<bbNG:selectOptionElement value="less" optionLabel="Less Than (numeric)" />
		</bbNG:selectElement>
		<bbNG:textElement name="searchTerm" onkeypress="return gcPreventEnter(event);" />
	</div>
</li>
<li id="combineSearchSection">
	<div class="label">
		<label for="field">
		Combine Search Criterias With
		</label>
	</div>
	<input type="radio" name="combinationOp" value="and" checked="checked" />And
	<input type="radio" name="combinationOp" value="or" />Or
</li>
<li id="searchButtonSection">
	<bbNG:button label="Search" onClick="gcShowUsersList(); return false;" />
	<span id="searchStatus"></span>
</li>
<li>
	<div id="userlists">
		<jsp:include page="userslist.jsp" flush="true">
			<jsp:param name="clearSearch" value="true" />
		</jsp:include>
	</div>
</li>
</ul>
</div>

<bbNG:jsBlock>
<script type="text/javascript">

	// Due to how group names being the unique identifier for groups, 
	// if the user gives a group name that is already taken,
	// it'll do a group merge with the selected users instead of 
	// creating a new group. Sometimes, this behaviour may be 
	// desired, so we'll just warn the user if they use a group name 
	// that already exists. 
	// 
	// We'll wait at least 1 second after the user stopped typing to check
	// if the group name is a duplicate. 
	var gcTimerRestart = false; // did the user type while timer active 
	var gcTimerGoing = false; // the timer is currently active 
	var gcGroupsList = ${gcGroupsList};
	
	function gcCheckName()
	{
		if (gcTimerGoing)
		{ // Mark the fact that the user typed something while timer active
			gcTimerRestart = true;
			return;
		}
		// no timer active, so activate one. 
		gcTimerGoing = true;
		setTimeout(
			function() 
			{
				gcTimerGoing = false;
				if (gcTimerRestart)
				{ // The user typed something while we were counting down,
					// so we need to start the timer again. 
					gcTimerRestart = false;
					gcCheckName();
				}
				else
				{ // User stopped typing, check for duplicate 
					if (gcGroupsList.indexOf($('name').getValue()) < 0)
					{ // no match, so not a duplicate, reset warning 
						$('gcNameWarning').update();
					}
					else
					{ // found a match, is a duplicate, display warning 
						$('gcNameWarning').update("WARNING: There is an existing group with the same name. If you continue, a new group will not be created, but selected users will be merged into the existing group instead.");
					}
				}
			}, 
			1000
		);
	}

	// hide the group name input box if we're adding to an existing group with the blackboard consumer. 
	if ($('blackboardConsumerOperation') != undefined)
	{
		gcHideNameIfMerge();
		$('blackboardConsumerOperation').observe('change', gcHideNameIfMerge);
	}
	
	function gcHideNameIfMerge()
	{
		if ($('blackboardConsumerOperation').getValue() == 'add')
		{
			$('gcGroupName').hide();
			if ($F('name') == "")
			{
				$('name').value = "EMPTY!!!--";
			}
		}
		else
		{
			$('gcGroupName').show();
			if ($F('name') == "EMPTY!!!--")
			{
				$('name').value = "";
			}
		}
	}
	
	// keeps track of how many criteria sections the user has created so far, we use this to uniquely id a criteria section 
	var criteriaCount = 0;
	$('combineSearchSection').hide();
	// add another search filter option
	function gcAddCriteria()
	{
		// need to give the whole search criteria an unique id so we can remove the whole section 
		// if the user turns out not to need this 
		var liId = "criteria_" + criteriaCount;
		// the html code for the self removal button 
		var removeButton = "<button type='button' onclick=\"$('"+ liId +"').remove(); return false;\" >Remove</button>";
		// insert the section at the bottom of the search criterias 
		$('combineSearchSection').insert({before: "<li id='" + liId + "'>" + $('searchOptions').innerHTML + removeButton + "</li>"});
		criteriaCount++;
		$('combineSearchSection').show();
	}
	
	// pressing enter after entering a search term usually triggers the submit button,
	// we want to trigger the search function instead. 
	function gcPreventEnter(event)
	{
		if (event.keyCode == 13)
		{
			event.preventDefault();
			gcShowUsersList();
			return false;
		}
	}
	
	// unfortunately, this function is necessary since just making the 'select all users'
	// checkbox checked does not trigger the event handler that checks all the boxes. 
	function gcSelectCheckboxes()
	{
		// first select the individual user checkboxes 
		var checkboxes = document.forms[0].getInputs('checkbox', 'usersSelected');
		checkboxes.each(
			function (box) { box.checked = true; }
		);
		// then select the select all users checkbox
		$('listContainer_selectAll').checked = true;
	}
	
	// perform the actual search function 
	function gcShowUsersList()
	{
		// lazy way to get all the search parameters, just grab 
		// ALL the form parameters.
		var form = $(document.forms[0]); // force IE to recognize Prototype DOM extensions 
		var params = form.serialize(true);
		// need to add course_id for BBL to know where it is 
		params.course_id = '<%=ctx.getCourseId().toExternalString() %>';
		new Ajax.Updater('userlists',
				'providers/groupcreator/userslist.jsp',
				{
					parameters : params,
					// enable js evaluation of the response or the 'select all' checkbox for inventoryList won't work 
					evalScripts : true,
					onCreate : function () { $('searchStatus').update("Searching, please wait..."); },
					onComplete : function () 
					{ 
						$('searchStatus').update(); 
						gcSelectCheckboxes();
					}
				});
	}
</script>
</bbNG:jsBlock>

</bbNG:includedPage>
