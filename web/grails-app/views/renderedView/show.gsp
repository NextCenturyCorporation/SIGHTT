<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'renderedView.label', default: 'RenderedView')}"/>
    <r:require module="thumbnail"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-renderedView" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                   default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="show-renderedView" class="content scaffold-show" role="main">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list renderedView">

        <g:if test="${renderedViewInstance?.filePath}">
            <li class="fieldcontain">
                <span id="filePath-label" class="property-label"><g:message code="renderedView.filePath.label"
                                                                            default="File Path"/></span>
                <span class="property-value" aria-labelledby="filePath-label"><g:fieldValue
                        bean="${renderedViewInstance}" field="filePath"/></span>
            </li>
        </g:if>

        <g:if test="${renderedViewInstance?.name}">
            <li class="fieldcontain">
                <span id="name-label" class="property-label"><g:message code="renderedView.name.label"
                                                                        default="Name"/></span>
                <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${renderedViewInstance}"
                                                                                        field="name"/></span>
            </li>
        </g:if>

        <g:if test="${renderedViewInstance?.sizeInMeters}">
            <li class="fieldcontain">
                <span id="sizeInMeters-label" class="property-label"><g:message code="renderedView.sizeInMeters.label"
                                                                                default="Size (m)"/></span>
                <span class="property-value" aria-labelledby="sizeInMeters-label"><g:fieldValue
                        bean="${renderedViewInstance}" field="sizeInMeters"/></span>
            </li>
        </g:if>

        <li class="fieldcontain">
            <span id="renderedImage-label" class="property-label"><g:message code="renderedView.renderedImage.label"
                                                                             default="File"/></span>
            <span class="property-value" aria-labelledby="renderedView-label"><a href="${fullImageSrc}"><div
                    id="insertThumbnail${renderedViewInstance.id}"><r:img uri="/images/spinner.gif"/></div></a></span>
        </li>

    </ol>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${renderedViewInstance?.id}"/>
            <g:link class="edit" action="edit" id="${renderedViewInstance?.id}"><g:message
                    code="default.button.edit.label" default="Edit"/></g:link>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>

<r:script>
            loadThumbnail("insertThumbnail${renderedViewInstance.id}","<g:createLink controller="renderedView"
                                                                                     action="thumbImgSrc"
                                                                                     id="${renderedViewInstance.id}"/>");
</r:script>

</body>
</html>
