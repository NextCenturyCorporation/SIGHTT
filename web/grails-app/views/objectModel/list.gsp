<%@ page import="com.ncc.sightt.ObjectModel" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'objectModel.label', default: '3D Model')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-objectModel" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                  default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <%--<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>--%>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
        <g:if test="${onlyMine == true}"><li><g:link action="list">View all visible</g:link></li></g:if>
        <g:else><li><g:link action="list" params="[owned: 'true']">View only mine</g:link></li></g:else>
    </ul>
</div>

<div id="list-objectModel" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>
            <th><g:message code="objectModel.thumbnail.label" default="Rendered Thumbnail"/></th>

            <g:sortableColumn property="name" title="${message(code: 'objectModel.name.label', default: 'Name')}"/>

            <g:sortableColumn property="modelType"
                              title="${message(code: 'objectModel.modelType.label', default: 'Model Type')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${objectModelInstanceList}" status="i" var="objectModelInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}"
                onclick='document.location = "<g:createLink action='show' id='${objectModelInstance.id}'/>" '>
                <td><img src="${objectModelThumbnailSrcMap[objectModelInstance.id]}"
                         alt="model ${objectModelInstance.id}"/></td>
                <td>${fieldValue(bean: objectModelInstance, field: "name")}</td>
                <td>${fieldValue(bean: objectModelInstance, field: "modelType")}</td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${objectModelInstanceTotal}"/>
    </div>
</div>
</body>
</html>
