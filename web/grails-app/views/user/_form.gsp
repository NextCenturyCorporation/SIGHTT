<%@ page import="com.ncc.sightt.auth.User" %>



<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} required">
    <label for="username"><g:message code="user.username.label"
                                     default="Username"/> <span class="required-indicator">*</span>
    </label>
    <g:textField name="username" required=""
                 value="${userInstance?.username}"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} required">
    <label for="email"><g:message code="user.email.label"
                                  default="Email"/> <span class="required-indicator">*</span>
    </label>
    <g:textField name="email" required=""
                 value="${userInstance?.email}"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'passwordHash', 'error')} ">
    <label for="password"><g:message
            code="user.password.label" default="New Password"/>

    </label>
    <g:textField name="password"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'permissions', 'error')} ">
    <label><g:message
            code="user.permissions.label" default="Permissions"/></label>
    <label>Permissions are not editable for users, please edit the role instead</label>
</div>

<div
        class="fieldcontain ${hasErrors(bean: userInstance, field: 'roles', 'error')} ">
    <label for="roles"><g:message code="user.roles.label"
                                  default="Roles"/>

    </label>
    <g:select name="roles" from="${com.ncc.sightt.auth.Role.list()}"
              multiple="multiple" optionKey="id" optionValue="name" size="5"
              value="${userInstance?.roles*.id}" class="many-to-many"/>

</div>

