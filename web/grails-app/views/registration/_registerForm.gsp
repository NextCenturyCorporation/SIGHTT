<%@ page import="com.ncc.sightt.auth.User" %>



<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} required">
    <label for="username"><g:message code="user.username.label"
                                     default="Username"/> <span class="required-indicator">*</span>
    </label>
    <g:textField name="username" pattern="[a-zA-Z0-9_-]+" required=""
                 placeholder="Enter a username"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} required">
    <label for="email"><g:message code="user.email.label"
                                  default="Email"/> <span class="required-indicator">*</span>
    </label>
    <g:textField name="email" pattern="^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9.-]+\\.)+[a-zA-Z]{2,4}\$"
                 required=""
                 placeholder="Enter an email address"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'passwordHash', 'error')} ">
    <label for="password"><g:message code="user.password.label" default="New Password"/><span
            class="required-indicator">*</span>

    </label>
    <g:passwordField pattern="^.{8,64}\$" name="password" required="" placeholder="Enter a password"/>
</div>

<div>
    <recaptcha:ifEnabled>
        <recaptcha:recaptcha theme="blackglass"/>
    </recaptcha:ifEnabled>

</div>