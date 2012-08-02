<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"
  %>
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage ctxId="ctx">

<bbNG:jspBlock>
<%
pageContext.setAttribute("bbContext", ctx); 
%>
</bbNG:jspBlock>

<style type="text/css">
#csvExportTabs
{
	margin-bottom: 0.5em;
}
#csvExportTabs li
{
	padding-top: 0.5em;
	padding-left: 1em;
	padding-right: 1em;
	float: left;
}
#csvExportTabs li input
{
	margin: 1px;
	margin-bottom: 0.5em;
}
#csvExportWindow
{
	padding: 1em;
}
.csvExportBold
{
	font-weight: bold;
}
.csvExportLight
{
	background: #e2e2e2;
}
.csvExportDark
{
	background: #cacaca;
}
#csvExportName
{
	width: 20em;
}
</style>
<div class="ie8hacks">
<ul id="csvExportTabs">
	<li id="csvExportDlLi">
		<!-- onclick is an IE hack to fix onchange not firing -->
		<input type="radio" name="csvExportOperation" value="download" id="csvExportDl" 
			checked="checked" 
			onchange="csvExportToggleOperation(); return false;" 
			onclick="this.blur(); this.focus();" 
			/> 
		<label id="csvExportDlLabel" for="csvExportDl">Download to Your Computer</label>
	</li>
	<li id="csvExportCsLi">
		<input type="radio" name="csvExportOperation" value="cs" id="csvExportCs" 
			onchange="csvExportToggleOperation(); return false;" 
			onclick="this.blur(); this.focus();"
			/> 
		<label id="csvExportCsLabel" for="csvExportCs">Save to Content Collection</label>
	</li>
</ul>

<ul id="csvExportWindow">
	<!-- Empty li to fix floating toggle -->
	<li>
	</li>
	<li>
		<div class="label">
			<label for="name">
			Output File Name
			</label>
		</div>
		<bbNG:textElement name="csvExportName" isRequired="true" value="${bbContext.course.displayTitle}.csv" />
	</li> 
	
	<li id="csvExportCSOption">
		<div class="label">
			<label for="csvExportCSFolder">
			Save in Content System Folder
			</label>
		</div>
		<bbNG:filePicker bypassStepCheck="true" baseElementName="csvExportCSFolder" var="csvExportFolder" 
			mode="INLINE_SINGLE_CS_FILE_ONLY" allowMultipleFiles="false" pickerMode="FOLDER">
		</bbNG:filePicker>
	</li>
</ul>
</div>

<bbNG:jsBlock>
<script type="text/javascript">

// hide the content system options on intial page load 
csvExportToggleOperation();

function csvExportToggleOperation()
{
	var select = $('csvExportCs');
	if (select.checked)
	{
		csvExportMarkSelectedOperation("csvExportCs", "csvExportDl");
		$('csvExportCSOption').show();
	}
	else
	{
		csvExportMarkSelectedOperation("csvExportDl", "csvExportCs");
		$('csvExportCSOption').hide();
	}
}

function csvExportMarkSelectedOperation(selected, other)
{
	$(selected + "Label").addClassName('csvExportBold');
	$(selected + "Li").addClassName('csvExportDark');
	$(selected + "Li").removeClassName('csvExportLight');
	$(other + "Label").removeClassName('csvExportBold');
	$(other + "Li").removeClassName('csvExportDark');
	$(other + "Li").addClassName('csvExportLight');
}
</script>
</bbNG:jsBlock>

</bbNG:includedPage>
