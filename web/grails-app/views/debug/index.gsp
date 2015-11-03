<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 3/19/14
  Time: 1:46 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>
</head>

<body>
<ul>
    <g:each var="action" in="${actions}">
        <li><g:link action="${action.key}">${action.key}</g:link></li>
    </g:each>
</ul>
</body>
</html>