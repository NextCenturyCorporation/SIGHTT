<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/12/13
  Time: 4:03 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Reset Your Password</title>
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
<g:if test="${flash.message}">
    <dif class="errors" role="status">${flash.message}</dif>
</g:if>
<g:else>
    <h1>Resetting password for user: ${user.username}</h1>
    <g:form action="doPasswordReset">
        <div class="fieldcontain required">
            <label for="newPassword"><g:message
                    code="user.new.password.label" default="New Password"/>  <span class="required-indicator">*</span>
            </label>
            <g:passwordField name="newPassword" required="" value="" placeholder="New Password"/>

        </div>

        <div class="fieldcontain required">
            <label for="confirmPassword"><g:message
                    code="user.confirm.password.label" default="Retype Password"/>  <span
                    class="required-indicator">*</span>
            </label>
            <g:passwordField name="confirmPassword" required="" value="" placeholder="Retype Password"
                             oninput="checkRetypePassword(this)"/>
            <span id="passwordStatus" role="status"></span>
        </div>
        <fieldset class="buttons">
            <g:submitButton name="reset" class="register"
                            value="${message(code: 'default.button.resetpassword.label', default: 'Reset Password')}"/>
        </fieldset>
        <g:hiddenField name="resetCode" value="${resetCode}"/>
        <!-- We cannot put the username here because otherwise the client could spoof it easily-->
    </g:form>
</g:else>
</body>
</html>