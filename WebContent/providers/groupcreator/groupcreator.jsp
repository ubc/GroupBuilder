<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"
  import="java.util.ArrayList,
  ca.ubc.ctlt.group.groupcreator.*,
  blackboard.data.user.User,
  blackboard.persist.user.UserDbLoader,
  blackboard.persist.Id,
  blackboard.persist.DataType
  "%>
<%@ taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:includedPage ctxId="ctx">
  
<ol>
<li class="required">
	<div class="label">
		<label for="name">
		<img src="/images/ci/icons/required.gif" alt="Required">
		Name
		</label>
	</div>
	<bbNG:textElement name="name" isRequired="true" />
</li> 
</ol>
<h3 class="steptitle">Search</h3>
<ol>
<li>
	<div class="label">
		<label for="field">
		Grade Center Field
		</label>
	</div>
	<bbNG:jspBlock>
     <%
        GradeCenterUtil gc = new GradeCenterUtil(ctx);
        out.print("<select name=\"field\" id=\"searchField\">");
        for (LineitemWrapper c : gc.getColumns())
        {
          out.print("<option value=" + c.getIdString() + ">" + c.getName() + "</option>");
        }
        out.print("</select>");
      %>
	</bbNG:jspBlock>
	<bbNG:selectElement name="op" id="searchOp" isRequired="true">
		<bbNG:selectOptionElement value="contains" optionLabel="Contains" />
		<bbNG:selectOptionElement value="exactly" optionLabel="Exactly" />
		<bbNG:selectOptionElement value="exclude" optionLabel="Exclude" />
		<bbNG:selectOptionElement value="greater" optionLabel="Greater Than (numeric)" />
		<bbNG:selectOptionElement value="equal" optionLabel="Equal (numeric)" />
		<bbNG:selectOptionElement value="less" optionLabel="Less Than (numeric)" />
	</bbNG:selectElement>
	<bbNG:textElement name="term" id="searchTerm" isRequired="true" />
</li>
<li>
	<bbNG:button label="Apply" onClick="showUsersList();return false;" />
</li>
<li>
	<div id="userlists">
		<jsp:include page="userslist.jsp" flush="true" />
	</div>
</li>
</ol>
  
	<bbNG:jsBlock>
        <script type="text/javascript">
        function showUsersList() {
	        var field = $('searchField').getValue().escapeHTML();
	        var op = $('searchOp').getValue().escapeHTML();
	        var term = $('searchTerm').getValue().escapeHTML();
	        new Ajax.Request( 'providers/groupcreator/userslist.jsp', {
	        	method: 'get',
	        	parameters: "course_id=_365_1&searchField=" + field +"&searchOp=" + op + "&searchTerm=" + term,
	        	onSuccess: 
	        		function( req )
		        	{
	        			$('userlists').innerHTML = req.responseText;
		        	}
	        	}
	        );
        }
        </script>
	</bbNG:jsBlock>

</bbNG:includedPage>

