<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName"
           value="${message(code: 'user.label', default: 'User')}"/>
    <title>User Accounts on SIGHTT</title>
</head>

<body>
<div id="list-user" class="content scaffold-list" role="main">
    <h1>User Accounts</h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">
            ${flash.message}
        </div>
    </g:if>
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="username" title="Username"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${userInstanceList}" status="i" var="userInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td><g:link action='showUser' id="${userInstance.id}">
                    ${userInstance.username}
                </g:link></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${userInstanceTotal}"/>
    </div>
</div>

</body>
</html>