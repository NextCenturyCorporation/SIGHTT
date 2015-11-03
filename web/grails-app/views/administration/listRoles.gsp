<html>
<head>
<meta name="layout" content="main">
<g:set var="entityName"
       value="${message(code: 'background.label', default: 'Background')}"/>
<title><g:message code="default.list.label" args="[entityName]"/></title>
</head>
<body>
<a href="#list-background" class="skip" tabindex="-1"><g:message
        code="default.link.skip.label" default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <%--<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>--%>
        <li><g:link class="create" action="create">
            <g:message code="default.new.label" args="[entityName]"/>
        </g:link></li>
    </ul>
</div>

<div id="list-background" class="content scaffold-list" role="main">
    <h1>
        <g:message code="default.list.label" args="[entityName]"/>
    </h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">
            ${flash.message}
        </div>
    </g:if>
    <table>
        <thead>
        <tr>
            <th class="sortable">Background File</th>
            <th class="sortable">Geometry</th>
            <g:sortableColumn property="name"
                              title="${message(code: 'background.name.label', default: 'Name')}"/>

        </tr>
        </thead>
        <tbody>
        <g:each in="${backgroundInstanceList}" status="i"
                var="backgroundInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action='show' id="${backgroundInstance.id}">
                    <img src="${backgroundThumbnailSrcMap[backgroundInstance.id]}"
                         alt="background ${backgroundInstance.id}"/>
                </g:link></td>
                <td><g:link action="drawing" controller="defineGeometry"
                            id="${backgroundInstance.id}">
                    <g:if test="${backgroundInstance.geometry}">
                        Edit
                    </g:if>
                    <g:else>
                        Add
                    </g:else>
                </g:link></td>
                <td>
                    ${fieldValue(bean: backgroundInstance, field: "name")}
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${backgroundInstanceTotal}"/>
    </div>
</div>

</body>
</body>
</html>