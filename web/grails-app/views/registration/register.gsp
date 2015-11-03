<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 10/29/13
  Time: 2:01 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="org.springframework.validation.FieldError" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Register for an Account on SIGHTT</title>
</head>


<body>
<h1><g:message code="default.register.message"/></h1>
<g:if test="${flash.message}">
    <div class="message" role="status">${flash.message}</div>
</g:if>
<recaptcha:ifFailed>
    <div id="faildiv" class="errors" role="status">
        CAPTCHA FAILED!!
    </div>
</recaptcha:ifFailed>
<g:form action="validateCaptcha">
    <fieldset class="form">
        <g:render template="registerForm"/>
    </fieldset>
    <fieldset class="buttons">
        <g:submitButton name="register" class="register"
                        value="${message(code: 'default.button.register.label', default: 'Register')}"/>
    </fieldset>

</g:form>
</div>
</body>
</html>