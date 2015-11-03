<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 2/18/14
  Time: 11:04 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup"/>

    <title>Camera States Set</title>
    <g:if test="${debug == false}">
        <r:script>window.close()</r:script>
    </g:if>
</head>

<body>
<button id="close" name="close" onclick="window.close();">Close Debug Window</button>
<table>
    <tr><th>Camera#</th><th>Active</th></tr>
    <g:each var="cam" status="num" in="${cameraStates}">
        <tr><td>${num}</td><td>${cam.active}</td></tr>
    </g:each>
</table>
</body>
</html>