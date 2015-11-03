<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/12/13
  Time: 3:20 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Password Reset</title>
</head>

<body>
<g:if test="${flash.message}">
    <div class="errors">${flash.message}</div>
</g:if>
<g:form action="sendPasswordReset">
    <table>
        <tbody>
        <tr>
            <td>Username:</td>
            <td><input type="text" name="username" value="${username}" placeholder="Enter your username"/></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="Reset Password"/></td>
        </tr>
        </tbody>
    </table>
</g:form>
</body>
</html>