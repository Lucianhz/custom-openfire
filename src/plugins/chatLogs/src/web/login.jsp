<%@page import="org.jivesoftware.util.JiveGlobals"%>
<%@page import="org.jivesoftware.openfire.XMPPServer"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>聊天插件</title>
</head>
<% 
	String domain = JiveGlobals.getProperty("xmpp.domain");
	String port = JiveGlobals.getProperty("adminConsole.port");
	String url = "http://" + domain + ":"+port+"/plugins/chatlogs/chatlogsservlet";
%>
<body>
	<form action="<%=url%>">
		群名称：<input type="text" name = "roomName" id="roomName"></input><br/>
		<input type="submit" value="提交">
	</form> 
	
	
</body>
</html>