<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'renderedView.label', default: 'RenderedView')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-renderedView" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                   default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="list-renderedView" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>

            <g:sortableColumn property="renderedImage"
                              title="${message(code: 'renderedView.renderedImage.label', default: 'Rendered View')}"/>
            <g:sortableColumn property="sizeInMeters"
                              title="${message(code: 'renderedView.sizeInMeters.label', default: 'Size (m)')}"/>
            <g:sortableColumn property="name" title="${message(code: 'renderedView.name.label', default: 'Name')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${renderedViewInstanceList}" status="i" var="renderedViewInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show" id="${renderedViewInstance.id}"><img
                        src="${renderedViewThumbnailSrcMap[renderedViewInstance.id]}"
                        alt="background ${renderedViewInstance.id}"/></g:link></td>
                <td>${fieldValue(bean: renderedViewInstance, field: "sizeInMeters")}</td>
                <td>${fieldValue(bean: renderedViewInstance, field: "name")}</td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${renderedViewInstanceTotal}"/>
    </div>
</div>
</body>
</html>
