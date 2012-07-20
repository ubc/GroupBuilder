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

<ul>
	<li>
		<div class="label">
			<label for="name">
			Output File Name
			</label>
		</div>
		<bbNG:textElement name="csvExportName" isRequired="true" value="${bbContext.course.displayTitle}.csv" />
	</li> 
	
	<li>
		<div class="label">
			<label for="name">
			Save Mode
			</label>
		</div>
		<bbNG:selectElement name="csvExportOperation" isRequired="true" onchange="csvExportToggleOperation(); return false;">
			<bbNG:selectOptionElement value="download" optionLabel="Download" />
			<bbNG:selectOptionElement value="cs" optionLabel="Save as Content System File" />
		</bbNG:selectElement>
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

<bbNG:jsBlock>
<script type="text/javascript">

// hide the content system options on intial page load 
csvExportToggleOperation();

function csvExportToggleOperation()
{
	var select = $('csvExportOperation');
	if (select[select.selectedIndex].value == "cs")
	{
		$('csvExportCSOption').show();
		showUsersList(); // need to init the group users display 
	}
	else
	{
		$('csvExportCSOption').hide();
	}
}
</script>
</bbNG:jsBlock>

</bbNG:includedPage>
