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
	The input file must be a comma delimited CSV file. Each value can be
	surrounded by quote marks. The first line is the column header which
	indicates the information stored for each column. There are 4 columns, 
	the first column is the Group name, the second column is Username, 
	the third is the student's ID and the last column indicates the 
	GroupSet name.
</p>

<ol>
	<li>
	Either the Username or Student ID column must exist to identify the student.
	Username may be omitted if Student ID is available and vice versa. If both columns
	are available, Username takes precedence.
	</li>
	<li>The GroupSet column is optional.</li>
</ol>
		
<p>E.g.:</p>
<pre id="sampleCSV">
"Group","Username","Student ID","GroupSet"
"Group1","Username0","10000000","Set1"
"Group1","Username1","10000001","Set1"
"Group2","Username2","10000002","Set1"
</pre>
</div>

</bbNG:includedPage>