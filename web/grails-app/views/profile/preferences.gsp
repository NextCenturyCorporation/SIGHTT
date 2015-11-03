<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/15/13
  Time: 11:13 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page import="com.ncc.sightt.auth.Permissions; com.ncc.sightt.auth.AllowedCommunications" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>User Preferences</title>
    <r:require module="jquery"/>
    <r:script>
        function updatePermsText() {
            $("#permsLabel").html($('#jobPrivacy').val());
        }
        $(function () {
            updatePermsText()
        });

    </r:script>
</head>

<body>

<div id="page-body" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <div id="commPrefs" class="preferences">
        <h1>Change your communication preferences</h1>
        <g:form controller="profile" action="preferences">
            <g:hiddenField name="group" value="communication"/>
            <ul>
                <g:each in="${AllowedCommunications.values()}" var="com">
                    <li>
                        <g:checkBox name="${com}"
                                    checked="${user.preferences.allowedCommunications.contains(com)}"/><label>${com.description}</label>
                    </li>
                </g:each>
                <li>
                    <g:submitButton name="submit" value="Update"/>
                </li>
            </ul>
        </g:form>
    </div>

    <div id="jobPrefs" class="preferences">
        <h1>Change default job preferences</h1>
        <g:form controller="profile" action="preferences">
            <g:hiddenField name="group" value="job"/>
            <ul>
                <li>
                    <g:select name="jobPrivacy" from="${Permissions}" optionValue="description"
                              value="${user.preferences.defaultPrivacy}"
                              onchange="updatePermsText()"/>
                    <label id="permsLabel"></label>
                </li>
                <li>
                    <g:submitButton name="submit" value="Update"/>
                </li>
            </ul>
        </g:form>
    </div>
</div>
</body>
</html>