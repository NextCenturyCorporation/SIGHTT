<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'jobTask.label', default: 'JobTask')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-jobTask" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="list-jobTask" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>

            <th><g:message code="jobTask.background.label" default="Background"/></th>

            <g:sortableColumn property="completed"
                              title="${message(code: 'jobTask.completed.label', default: 'Completed')}"/>

            <g:sortableColumn property="completedTime"
                              title="${message(code: 'jobTask.completedTime.label', default: 'Completed Time')}"/>

            <th><g:message code="jobTask.compositeImage.label" default="Composite Image"/></th>

            <th><g:message code="jobTask.renderedView.label" default="Rendered View"/></th>

            <g:sortableColumn property="startTime"
                              title="${message(code: 'jobTask.startTime.label', default: 'Start Time')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${jobTaskInstanceList}" status="i" var="jobTaskInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action="show"
                            id="${jobTaskInstance.id}">${fieldValue(bean: jobTaskInstance, field: "background")}</g:link></td>

                <td><g:formatBoolean boolean="${jobTaskInstance.completed}"/></td>

                <td><g:formatDate date="${jobTaskInstance.completedTime}"/></td>

                <td>${fieldValue(bean: jobTaskInstance, field: "compositeImage")}</td>

                <td>${fieldValue(bean: jobTaskInstance, field: "renderedView")}</td>

                <td><g:formatDate date="${jobTaskInstance.startTime}"/></td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${jobTaskInstanceTotal}"/>
    </div>
</div>
</body>
</html>
