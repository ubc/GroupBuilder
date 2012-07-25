<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" %>
	
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage>

<style type="text/css">
#sampleCSV
{
	font-family: monospace;
	margin: 1em 1em 1em 3em;
	font-size: 1.5em;
}
#startCSVExp
{
	margin-top: 2em;
}
</style>

<!-- <input type='file' name='csvfile'> -->

<bbNG:filePicker bypassStepCheck="true" baseElementName="csvfile" var="csvfile">
</bbNG:filePicker>

<div class="ie8hacks">
<p id="startCSVExp">
The input file must be a comma delimited CSV file. Each value can be surrounded by quote marks. The first line is 
the header and is ignored during processing. There are 3 columns, the first column indicates the GroupSet name,
the second column is the Group name, and the third column is the student's ID.
</p>
<p>
E.g.:
</p>
<pre id="sampleCSV">
"GroupSet","Group","Student ID"
"Set1","Group1","10000000"
"Set1","Group1","10000001"
"Set1","Group2","10000002"
</pre>
</div>

</bbNG:includedPage>