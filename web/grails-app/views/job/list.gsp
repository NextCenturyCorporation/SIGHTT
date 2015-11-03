<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'job.label', default: 'Job')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<a href="#list-job" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                          default="Skip to content&hellip;"/></a>
<%--<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
    </ul>
</div>--%>
<div id="list-job" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="jobName" title="${message(code: 'job.jobName.label', default: 'Name')}"/>
            <g:sortableColumn property="owner" title="${message(code: 'job.owner.label', default: 'User')}"/>
            <g:sortableColumn property="numImages"
                              title="${message(code: 'job.config.numImages.label', default: 'Images')}"/>
            <g:sortableColumn property="submitDate"
                              title="${message(code: 'job.submitDate.label', default: 'Submit Date')}"/>
            <g:sortableColumn property="expectedEndDate"
                              title="${message(code: 'job.expectedEndDate.label', default: 'Expected End Date')}"/>
            <g:sortableColumn property="actualEndDate"
                              title="${message(code: 'job.actualEndDate.label', default: 'Actual End Date')}"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${jobInstanceList}" status="i" var="jobInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}"
                onclick='document.location = "<g:createLink action='show' id='${jobInstance.id}'/>" '>
                <td>${fieldValue(bean: jobInstance, field: "jobName")}</td>
                <td>${fieldValue(bean: jobInstance, field: "owner.username")}</td>
                <td>${fieldValue(bean: jobInstance, field: "config.numImages")}</td>
                <td><g:formatDate format="yyyy-MM-dd HH:mm z" date="${jobInstance.submitDate}"/></td>
                <td><g:formatDate format="yyyy-MM-dd HH:mm z" date="${jobInstance.expectedEndDate}"/></td>
                <td><g:formatDate format="yyyy-MM-dd HH:mm z" date="${jobInstance.actualEndDate}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${jobInstanceTotal}"/>
    </div>
</div>
</body>
</html>
