<html>
<head>

    <title>SIGHTT Wizard &raquo; <g:layoutTitle default="Welcome"/></title>
    <link rel="stylesheet"
          href="${resource(dir: 'css', file: 'wizard.css')}"/>
    <g:layoutHead/>
    <meta name="layout" content="main"/>
</head>

<body>
<%--<div class="container navbar">
    <ul>
        <li><g:link controller="main" action="index">Home</g:link></li>
        <li><g:link controller="Wizard" action="index">Start the Wizard</g:link></li>
        <li><g:link controller="Job" action="index">Current Jobs</g:link></li>
        <li><g:link controller="objectModel" action="create">Upload Model</g:link></li>
        <li><g:link controller="BuildInfo">Build Info</g:link></li>
        <li><g:link controller="main" action="faq">FAQ</g:link></li>
    </ul>
</div>--%>

<div id="container">

    <div id="step" class="container">
        <g:pageProperty name="page.step"/>
    </div>

    <div id="main" class="container">

        <div id="description">
            <div class="message" role="status">
                <g:pageProperty name="page.description"/>
            </div>
        </div>

        <g:pageProperty name="page.summary"/>

        <div class="ui-row">
            <div id="modelcontainer" class="ui-block">
                <div id="progressbar"></div>

                <g:pageProperty name="page.cameraLoading"/>

                <div id="modelview"></div>
                <g:pageProperty name="page.afterModelview"/>
            </div>

            <div id="modelview-controls" class="ui-block">
                <g:pageProperty name="page.controls"/>
            </div>
        </div>

        <div id="nav">
            <g:pageProperty name="page.nav"/>
        </div>
    </div>

</body>
</html>