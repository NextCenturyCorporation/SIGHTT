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
    <title>Change Password</title>
    <r:require module="jquery"/>
    <r:script>
        function checkRetypePassword(input) {
            if (input.value != $('#newPassword').val()) {
                input.setCustomValidity("Passwords do not match!");
                $('#passwordStatus').removeClass();
                $('#passwordStatus').addClass("errors");
                $('#passwordStatus').html("Passwords do not match!");
            } else {
                input.setCustomValidity("");
                $('#passwordStatus').removeClass();
                $('#passwordStatus').addClass("confirm");
                $('#passwordStatus').html("Passwords match");
            }
        }

    </r:script>
</head>

<body>
<p>Use this form to change your password</p>

<div id="page-body" role="main">
    <g:if test="${flash.message}">
        <div class="errors" role="status">${flash.message}</div>
    </g:if>
    <g:form action="changePassword">
        <fieldset class="form">
            <g:render template="passwordForm"/>
        </fieldset>
        <fieldset class="buttons">
            <g:submitButton name="register" class="register"
                            value="${message(code: 'default.button.changepassword.label', default: 'Change Password')}"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>