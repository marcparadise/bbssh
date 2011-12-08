<?xml version="1.0"?>
<!DOCTYPE wml PUBLIC "-//WAPFORUM//DTD WML 1.3//EN" "http://www.wapforum.org/DTD/wml13.dtd">
<%@page import="org.bbssh.onlinehelp.model.HelpTopic,org.bbssh.onlinehelp.model.HelpTopicDetail" %>
<%@page contentType="text/vnd.wap.wml"%>
<%
	// Note: WML 1.3 is supported back to the 8700 8350.  I think that
	// covers the oldest versions of the OS that we support...
	// This simple page renders WML cards - one for each
	// entry for this topicin Help.

	// Note also... For future, the official spec is XHTML...
	HelpTopic topic = (HelpTopic)request.getAttribute("HelpTopic");
%>
<wml>
<%
		int x = 0;
		int count = topic.getHelpTopicDetailCollection().size();
		for (HelpTopicDetail detail : topic.getHelpTopicDetailCollection()) {
%>
	<card id="A<%=x%>" title="BBSSH Help: <%=topic.getName()%>">
		<p><%=detail.getText()%></p>
		<p>
			<% if (x > 0) { %> 
				<anchor>Prev<prev /></anchor>
			<% }%>
			<% if (x < (count - 1)) {  %>  
				<anchor> <a href="#A<%=(x + 1)%>">Next</a></anchor>
			<% }%>
		</p>
		<% x++; %>
	</card>
	<% }%>
</wml>