<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 10/29/13
  Time: 3:23 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Registration Successful!</title>
</head>

<body>

<g:if test="${flash.message}">
    <h1>Registration Failed</h1>

    <div class="errors" role="status">${flash.message}</div>
</g:if>
<g:else>

    <div class="message"
         role="status">Your registration has been sent to a SIGHTT administrator who will review your request within 2 business days.<br/>
        Once your account has been activated, you will receive an email.</div>

    <p class="confirm">Your IP address is recorded to prevent malicious use of the site</p>

    <div id="page-body" role="main">
        <h1><g:message code="default.register.successful.message"/></h1>
        <table>
            <th colspan="2">Account Information</th>
            <tr><td>Username</td><td>${user.username}</td></tr>
            <tr><td>Email</td><td>${user.email}</td></tr>
            <tr><td>IP Address</td><td>${user.registrationIp}</td></tr>
        </table>


        <div class="BigButton"><g:link controller="main" action="index">Return to Home</g:link></div>
    </div>
</g:else>
</body>
</html>