<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 11/8/13
  Time: 9:21 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Profile for ${user.username}</title>
    <meta name="layout" content="main"/>
</head>

<body>
<p>Your current profile is below, click on your email or password to change them.</p>

<div id="page-body" role="main">
    <div id="profileData">
        <ul>
            <li>
                <span class="profile-label">Username:</span><span class="profile-value">${user.username}</span>
            </li>
            <li>
                <span class="profile-label">Email:</span><span class="profile-value"><g:link
                    action="email">${user.email}</g:link></span>
            </li>
            <li>
                <span class="profile-label">Password:</span><span class="profile-value"><g:link
                    action="password">Change your password</g:link></span>
            </li>
            <li>
                <span class="profile-label">Preferences:</span><span class="profile-value"><g:link
                    action="preferences">Change your preferences</g:link></span>
            </li>
            <li>
                <span class="profile-label">Backgrounds:</span><span class="profile-value"><g:link
                    controller="background" action="list"
                    params="[owned: 'true']">Click to view your backgrounds</g:link></span>
            </li>
            <li>
                <span class="profile-label">Models:</span><span class="profile-value"><g:link
                    controller="background" action="list"
                    params="[owned: 'true']">Click to view your models</g:link></span>
            </li>
        </ul>
    </div>
</div>
</body>
</html>