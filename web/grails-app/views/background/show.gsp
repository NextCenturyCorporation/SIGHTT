<%@ page import="com.ncc.sightt.auth.Permissions" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'background.label', default: 'Background')}"/>
    <r:require module="thumbnail"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-background" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                 default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <%--<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>--%>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="show-background" class="content scaffold-show" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>

    <ol class="property-list background">

        <g:if test="${backgroundInstance?.name}">
            <li class="fieldcontain">
                <span id="name-label" class="property-label"><g:message code="background.name.label"
                                                                        default="Background Name"/></span>

                <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${backgroundInstance}"
                                                                                        field="name"/></span>

            </li>
        </g:if>
        <shiro:hasRole name="admin">
            <g:if test="${backgroundInstance?.filePath}">
                <li class="fieldcontain">
                    <span id="filePath-label" class="property-label"><g:message code="background.filePath.label"
                                                                                default="File Path"/></span>

                    <span class="property-value" aria-labelledby="filePath-label"><g:fieldValue
                            bean="${backgroundInstance}"
                            field="filePath"/></span>

                </li>
            </g:if>
        </shiro:hasRole>

        <g:if test="${backgroundInstance?.owner}">
            <li class="fieldcontain">
                <span id="owner-label" class="property-label"><g:message code="background.owner.label"
                                                                         default="Owner"/></span>

                <span class="property-value" aria-labelledby="owner-label">${backgroundInstance.owner.username}</span>

            </li>
        </g:if>

        <li class="fieldcontain">
            <span id="dimensions-label" class="property-label"><g:message code="background.dimensions.label"
                                                                          default="Dimensions (WxH)"/></span>
            <span class="property-value" aria-labelledby="dimensions-label"><g:fieldValue bean="${backgroundInstance}"
                                                                                          field="width"/> x <g:fieldValue
                    bean="${backgroundInstance}" field="height"/></span>

        </li>
        <li class="fieldcontain">
            <span id="backgroundFile-label" class="property-label"><g:message code="background.backgroundFile.label"
                                                                              default="File"/></span>
            <span class="property-value" aria-labelledby="backgroundFile-label"><g:link controller="imageDisplay"
                                                                                        action="display"
                                                                                        params='[filePath: "${backgroundInstance.filePath}"]'><div
                        id="backThumbnail${backgroundInstance.id}"><r:img uri="/images/spinner.gif"/></div></g:link>
            </span>

        </li>
        <li class="fieldcontain">
            <span id="permissions-label" class="property-label"><g:message code="background.permissions.label"
                                                                           default="Permissions"/></span>
            <span class="property-value" aria-labelledby="permissions-label"><g:fieldValue bean="${backgroundInstance}"
                                                                                           field="permissions"/></span>
            <g:form action="togglePermissions" id="${backgroundInstance.id}">
                <fieldset class="buttons">
                    <g:if test="${backgroundInstance.permissions == Permissions.PRIVATE}">
                        <g:actionSubmit name="togglePerms" value="Make Public" action="togglePermissions"/>
                    </g:if>
                    <g:else>
                        <g:actionSubmit name="togglePerms" value="Make Private" action="togglePermissions"/>
                    </g:else>
                </fieldset>
            </g:form>

        </li>

    </ol>
    <shiro:hasRole name="admin">
        <g:form>
            <fieldset class="buttons">
                <g:hiddenField name="id" value="${backgroundInstance?.id}"/>
                <g:link class="edit" action="edit" id="${backgroundInstance?.id}"><g:message
                        code="default.button.edit.label" default="Edit"/></g:link>
                <g:actionSubmit class="delete" action="delete"
                                value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
            </fieldset>
        </g:form>
    </shiro:hasRole>
</div>

<r:script>
    loadThumbnail("backThumbnail${backgroundInstance.id}","<g:createLink controller="background" action="thumbImgSrc"
                                                                         id="${backgroundInstance.id}"/>");
</r:script>
</body>
</html>
