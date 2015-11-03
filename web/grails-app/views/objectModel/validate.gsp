<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'objectModel.label', default: '3D Model')}"/>
    <r:require module="limitedaspects"/>
    <r:require module="thumbnail"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
    <r:script>
        runWhenModelInfoIsReady = function() {
            var sceneConfig = new SceneConfig();
            sceneConfig["showShadows"]=false;
            modelView = new ModelView("modelview",sceneConfig);
            modelView.setSrcPath("${createLink(action: 'index', controller: 'main', absolute: true)}js/lib/modelview");
            modelView.loadModel("${modelLocation}");
        }
    </r:script>

    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-objectModel" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                  default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="ModelInfo" class="fieldcontain">
    <div>
        <b>Model Name:</b> &nbsp <g:fieldValue bean="${objectModelInstance}" field="name"/>
    </div>

    <div>
        <b>Model Type:</b> &nbsp <g:fieldValue bean="${objectModelInstance}" field="modelType"/>
    </div>
</div>

<div id="RenderInfo" class="fieldcontain">
    <p>&nbsp</p>

    <p><r:img uri="/images/spinner.gif"/>Validating Model...</p>
</div>

<r:script>
            getModelInfo("RenderInfo","<g:createLink controller="objectModel" action="getModelInfo"
                                                     id="${objectModelInstance.id}"/>","modelImage","<g:createLink
        controller="objectModel" action="fullImgDisplaySrc" id="${objectModelInstance.id}"/>");
</r:script>
</body>
</html>
