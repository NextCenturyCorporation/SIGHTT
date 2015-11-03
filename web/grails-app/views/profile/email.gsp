<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/8/13
  Time: 9:34 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Change Email</title>
</head>

<body>
<p>Use this form to change your email address</p>

<div id="page-body" role="main">
    <g:if test="${flash.message}">
        <div class="errors" role="status">${flash.message}</div>
    </g:if>
    <g:form action="changeEmail">
        <fieldset class="form">
            <g:render template="emailForm" bean="${currentEmail}"/>
        </fieldset>
        <fieldset class="buttons">
            <g:submitButton name="register" class="register"
                            value="${message(code: 'default.button.changeemail.label', default: 'Change Email')}"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>