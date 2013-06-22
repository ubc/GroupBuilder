<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="ca.ubc.ctlt.group.*,java.util.*,blackboard.platform.*,blackboard.data.user.*,blackboard.persist.*,blackboard.persist.user.*,blackboard.data.course.*,blackboard.persist.course.*,blackboard.data.content.*,blackboard.base.*"%>

<!-- Tag libraries -->
<%@taglib prefix="bbNG" uri="/bbNG"%>

<bbNG:learningSystemPage ctxId="ctx" entitlement="course.groups.CREATE">
	
	<bbNG:pageHeader instructions="Please Follow the Steps">
		<bbNG:pageTitleBar iconUrl="" showIcon="false" showTitleBar="true" 
		title="GroupBuilder"/>
	</bbNG:pageHeader>
<style>
.container
{
	background: white; /* IE Background RGB Bug Fix */
}
</style>
<!--[if (lt IE 9)]>
<style type="text/css">
/* IE8 CSS hacks */
.ie8hacks
{
	margin-left: 3em;
}
</style>
<![endif]-->

	<bbNG:form action="process" method="post" onSubmit="return validateForm();" enctype="${enctype}">
		<bbNG:dataCollection>
		
			<bbNG:step title="Group Source - ${provider.description}">
				<jsp:include page="${provider.optionsPage}" flush="true" />
			</bbNG:step>
			
			<bbNG:step title="Group Destination - ${consumer.description}">
				<jsp:include page="${consumer.optionsPage}" flush="true" />
			</bbNG:step>
			
			<input type="hidden" name="provider" value="${provider.class.name}" />
			<input type="hidden" name="consumer" value="${consumer.class.name}" />
			<input type="hidden" name="course_id" value="${courseId}" />
			
			<bbNG:stepSubmit/>
		
		</bbNG:dataCollection>
	</bbNG:form>
	
</bbNG:learningSystemPage>