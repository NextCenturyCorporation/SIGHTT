<%@ page import="com.ncc.sightt.auth.Permissions; com.ncc.sightt.ObjectModel" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'objectModel.label', default: '3D Model')}"/>
    <r:require module="limitedaspects"/>
    <r:require module="thumbnail"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>

    <r:script>
        window.onload = function() {
            var sceneConfig = new SceneConfig();
            sceneConfig["showShadows"]=false;
            modelView = new ModelView("modelview",sceneConfig);
            modelView.setSrcPath("${createLink(action: 'index', controller: 'main', absolute: true)}js/lib/modelview");
            modelView.loadModel("${modelLocation}");
        }
    </r:script>
</head>

<body>
<a href="#show-objectModel" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                  default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="show-objectModel" class="content scaffold-show" role="main">

    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list objectModel">

        <g:if test="${objectModelInstance?.name}">
            <li class="fieldcontain">
                <span id="name-label" class="property-label"><g:message code="objectModel.name.label"
                                                                        default="Model Name"/></span>

                <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${objectModelInstance}"
                                                                                        field="name"/></span>

            </li>
        </g:if>
        <shiro:hasRole name="admin">

            <g:if test="${objectModelInstance?.filePath}">
                <li class="fieldcontain">
                    <span id="filePath-label" class="property-label"><g:message code="objectModel.filePath.label"
                                                                                default="File Path"/></span>

                    <span class="property-value" aria-labelledby="filePath-label"><g:fieldValue
                            bean="${objectModelInstance}" field="filePath"/></span>

                </li>
            </g:if>
        </shiro:hasRole>

        <g:if test="${objectModelInstance?.owner}">
            <li class="fieldcontain">
                <span id="owner-label" class="property-label"><g:message code="objectModel.owner.label"
                                                                         default="Owner"/></span>

                <span class="property-value" aria-labelledby="owner-label">${objectModelInstance.owner.username}</span>

            </li>
        </g:if>

        <g:if test="${objectModelInstance?.modelType}">
            <li class="fieldcontain">
                <span id="modelType-label" class="property-label"><g:message code="objectModel.modelType.label"
                                                                             default="Model Type"/></span>

                <span class="property-value" aria-labelledby="modelType-label"><g:fieldValue
                        bean="${objectModelInstance}" field="modelType"/></span>

            </li>
        </g:if>

        <g:if test="${objectModelInstance?.thumbnail}">
            <li class="fieldcontain">
                <span id="thumbnail-label" class="property-label"><g:message code="objectModel.thumbnail.label"
                                                                             default="Rendered Thumbnail"/></span>

                <span class="property-value" aria-labelledby="thumbnail-label">
                    <div id="objectLink${objectModelInstance.id}"><r:img uri="/images/spinner.gif"/></div></span>
            </li>
        </g:if>

        <li class="fieldcontain">
            <span id="permissions-label" class="property-label"><g:message code="objectModel.permissions.label"
                                                                           default="Permissions"/></span>
            <span class="property-value" aria-labelledby="permissions-label"><g:fieldValue bean="${objectModelInstance}"
                                                                                           field="permissions"/></span>
            <g:form action="togglePermissions" id="${objectModelInstance.id}">
                <fieldset class="buttons">
                    <g:if test="${objectModelInstance.permissions == Permissions.PRIVATE}">
                        <g:actionSubmit name="togglePerms" value="Make Public" action="togglePermissions"/>
                    </g:if>
                    <g:else>
                        <g:actionSubmit name="togglePerms" value="Make Private" action="togglePermissions"/>
                    </g:else>
                </fieldset>
            </g:form>

        </li>

    </ol>

    <div id="modelcontainer">
        <div id="progressbar"></div>

        <div id="modelview"></div>
    </div>

    <shiro:hasRole name="admin">
        <g:form>
            <fieldset class="buttons">
                <g:hiddenField name="id" value="${objectModelInstance?.id}"/>
                <g:link class="edit" action="edit" id="${objectModelInstance?.id}"><g:message
                        code="default.button.edit.label" default="Edit"/></g:link>
                <g:actionSubmit class="delete" action="delete"
                                value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
            </fieldset>
        </g:form>
    </shiro:hasRole>
</div>
<r:script>
          fixLink("objectThumbnail${objectModelInstance.id}","<g:createLink controller="objectModel"
                                                                            action="thumbImgSrc"
                                                                            id="${objectModelInstance.id}"/>","objectLink${objectModelInstance.id}","<g:createLink
        action="fullImgDisplayLink" id="${objectModelInstance.id}"/>");
</r:script>
</body>
</html>
