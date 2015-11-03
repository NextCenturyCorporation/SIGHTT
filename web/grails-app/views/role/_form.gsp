<%@ page import="java.nio.file.attribute.UserDefinedFileAttributeView" %>
<%@ page import="com.ncc.sightt.auth.Role" %>



<div
        class="fieldcontain ${hasErrors(bean: roleInstance, field: 'name', 'error')} required">
    <label for="name"><g:message code="role.name.label"
                                 default="Name"/> <span class="required-indicator">*</span>
    </label>
    <g:textField name="name" required="" value="${roleInstance?.name}"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: roleInstance, field: 'permissions', 'error')} ">
    <label for="permissions"><g:message
            code="role.permissions.label" default="Permissions"/>
    </label>
    <g:textArea name="permissions" value="${permissionsText}"/>

</div>

<%--<div
	class="fieldcontain ${hasErrors(bean: roleInstance, field: 'users', 'error')} ">
	<label for="users"> <g:message code="role.users.label"
			default="Users" />

	</label>
	<g:select name="users" from="${userList }" multiple="true"
		value="${userInstance?.users }" optionKey="id" />

--%></div>

