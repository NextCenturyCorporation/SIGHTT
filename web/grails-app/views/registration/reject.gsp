<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 10/31/13
  Time: 12:04 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Registration Rejected</title>
</head>

<body>
<g:if test="${flash.message}">
    <div class="errors" role="status">${flash.message}</div>

    <p>Please contact a SIGHTT administrator for assistance.</p>
</g:if>
<g:else>
    <h1><g:message code="default.activation.rejected.message"/></h1>

    <p>Registration for the following user has been rejected!</p>

    <p>Username: ${user.username}</p>

    <p>Email: ${user.email}</p>
</g:else>
</body>
</html>