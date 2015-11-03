<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>SIGHTT Administration</title>
</head>

<body>
<g:if test="${flash.message}">
    <div class="message">
        ${flash.message}
    </div>
</g:if>
<h1>Welcome to the administration page</h1>

<p>You can view/update the following</p>
<ul>
    <li><g:link action="listUsers">Users</g:link></li>
    <li><g:link action="listRoles">Roles</g:link></li>

</ul>
</body>
</html>