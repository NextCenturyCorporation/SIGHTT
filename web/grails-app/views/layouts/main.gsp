<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="SIGHTT"/></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}?v=2" type="image/x-icon">
    <link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
    <link href='//fonts.googleapis.com/css?family=Open+Sans:400,300,700' rel='stylesheet' type='text/css'/>
    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>
<div id="sighttLogo" role="banner">
    <a href="${createLink(uri: '/')}"><img
            src="${resource(dir: 'images', file: 'sightt-banner.png')}"
            alt="SIGHTT"/></a>

    <div class="navbar">
        <ul>

            <li><g:link controller="Wizard" action="index">Start Wizard</g:link></li>
            <li><g:link controller="Job" action="index">Jobs</g:link></li>
            <li><p>Upload</p>
                <ul>
                    <li><g:link controller="objectModel" action="create">Upload Model</g:link></li>
                    <li><g:link controller="background" action="create">Upload Background</g:link></li>
                </ul>
            </li>
            <li><g:link controller="main" action="faq">FAQ</g:link></li>
            <li><p>Other Sites</p>
                <ul>
                    <li><a href="https://www.sightt.com/forum">Forums</a></li>
                    <li><a href="https://www.sightt.com/wiki">Wiki</a></li>
                </ul>
            </li>
            <shiro:user>
                <li><p>${org.apache.shiro.SecurityUtils.subject.principal}</p>
                    <ul>
                        <li><g:link controller="profile" action="index">My Profile</g:link></li>
                        <li><g:link controller="background" action="list"
                                    params="[owned: 'true']">My&nbsp;Backgrounds</g:link></li>
                        <li><g:link controller="objectModel" action="list"
                                    params="[owned: 'true']">My&nbsp;Models</g:link></li>
                        <li><g:link controller="auth" action="signOut">Logout</g:link></li>
                    </ul>
                </li>
            </shiro:user>
            <shiro:notUser>
                <li><g:link controller="auth" action="login">Login</g:link>
                    <ul>
                        <li><g:link controller="registration" action="register">Register</g:link></li>
                        <li><g:link controller="auth" action="forgotPassword">Forgot&nbsp;Password</g:link></li>
                    </ul>
                </li>
            </shiro:notUser>
        </ul>
    </div>
</div>




<g:layoutBody/>


<div class="footer" role="contentinfo">
    The SIGHTT Project, 2014. Version <g:meta name="app.version"/>. <g:link
            controller="BuildInfo">Latest Build Info</g:link>
</div>

<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
<r:require module="application"/>
<r:layoutResources/>
</body>
</html>
