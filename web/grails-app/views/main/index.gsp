<html>
<head>
    <meta name="layout" content="main">
    <title>Welcome to SIGHTT</title>
</head>

<body id="welcome">
<%--
<h2>
    <g:message code="Welcome to SIGHTT" />
</h2>
--%>

<%--
<div id="page-body" class="navbar" role="main">
    <ul>
        <li class="active"><g:link controller="main" action="index">Home</g:link></li>
        <li><g:link controller="Wizard" action="index">Start the Wizard</g:link></li>
        <li><g:link controller="Job" action="index">Current Jobs</g:link></li>
        <li><g:link controller="objectModel" action="create">Upload Model</g:link></li>
        <li><g:link controller="BuildInfo">Build Info</g:link></li>
        <li><g:link controller="main" action="faq">FAQ</g:link></li>
    </ul>
</div>
--%>

<div id="page-body" role="main">
    <p>Welcome to the Synthetic Image Generation Harness for Training
    and Testing (SIGHTT) web page. The purpose of the SIGHTT system is to
    permit object recognition researchers to generate a large collection
    of ground-truthed synthetic images that span a wide range of
    operational conditions. Using these images, researchers and
    practitioners can both train classifiers (e.g., object detectors) and
    evaluate their object recognition algorithms on very large sets of
    input data.</p>

</div>

<div id="page-body" role="main">
    <p>The SIGHTT Wizard allows you to choose a background and a 3D
    model and generate synthetic imagery from them.</p>

    <div class="BigButton"><g:link controller="Wizard" action="index">Start the Wizard</g:link></div>
</div>

<%--    <div id="page-body" role="main">
    <p>We recommend using the wizard interface, but if you prefer to
        use the older advanced interface you may do so below.</p>

    <p>Instructions: View the backgrounds and object models to
        determine what backgrounds and objects that you want to use. Then
        select Jobs to create a new Job to insert those objects into the
        backgrounds.</p>
</div>

<div id="page-body" role="main">
    <ul>
        <li id="star"><g:link controller="Background" action="index">Add or view Backgrounds</g:link></li>
        <li id="star"><g:link controller="ObjectModel" action="index">Add or view Object Models</g:link></li>
        <li id="star"><g:link controller="Job" action="index">Add or view current Jobs</g:link></li>
    </ul>
</div>
--%>

</body>
</html>
