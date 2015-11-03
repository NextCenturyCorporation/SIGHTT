<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/8/13
  Time: 10:25 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Email Changed Successfully</title>
</head>

<body>
<div id="page-body" role="main">
    <p>Your email was successfully changed to: ${currentEmail}</p>
    <g:link action="index">Return to your profile</g:link>
</div>

</body>
</html>