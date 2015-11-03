<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 10/30/13
  Time: 11:53 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Activate Account</title>
    <g:set var="username" value="${user.username}"/>
</head>

<body>

<g:if test="${flash.message}">
    <div class="errors" role="status">${flash.message}</div>

    <p>Please contact a SIGHTT administrator for assistance.</p>
</g:if>
<g:else>

    <div class="message"
         role="status">Registration for the following user has been activated.<br/>An email has been sent to the user to alert them.
    </div>

    <div id="page-body" role="main">
        <h1><g:message code="registration.activation.sucecssful" args="[username]"/></h1>
        <table>
            <th colspan="2">Account Information</th>
            <tr><td>Username</td><td>${user.username}</td></tr>
            <tr><td>Email</td><td>${user.email}</td></tr>
        </table>

        <div class="BigButton"><g:link controller="main" action="index">Return to Home</g:link></div>
    </div>

</g:else>
</body>
</html>