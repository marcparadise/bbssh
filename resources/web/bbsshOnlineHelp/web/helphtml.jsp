<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.bbssh.onlinehelp.model.HelpTopic,org.bbssh.onlinehelp.model.HelpTopicDetail" %>
<%
		// This simple page renders WML cards - one for each
		// entry for this topicin Help.
		HelpTopic topic = (HelpTopic)request.getAttribute("HelpTopic");
%>
<html>
	<head>
		<title>BBSSH Help: <%=topic.getName()%></title>
	</head>
	<body>
	<%
		int x = 0;
		int count = topic.getHelpTopicDetailCollection().size();
		for (HelpTopicDetail detail : topic.getHelpTopicDetailCollection()) {
	%>
		<div class=".helpContent">
			<%=detail.getText()%>
		</div>
	<%x++;%>
	<%}%>
	</body>
</html>

