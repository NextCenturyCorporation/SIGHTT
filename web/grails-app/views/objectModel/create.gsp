<%@ page import="com.ncc.sightt.ObjectModel" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'objectModel.label', default: '3D Model')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
    <r:require module="objectModelCreate"/>
    <r:script>
            $('#createButton').prop("disabled",true);
            $("#uploadText").attr("disabled", "disabled");
            createObjectModelDropzone("${createLink(controller: 'objectModel', action: 'upload')}");
    </r:script>
</head>

<body>
<a href="#create-objectModel" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                                    default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <%--<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>--%>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="create-objectModel" class="content scaffold-create" role="main">
    <h1><g:message code="default.create.label" args="[entityName]"/></h1>

    <p>Please see the FAQ for a discussion of the 3D Model requirements.  Currently,
    only Blender models are supported.</p>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <div class="message"
         role="status">When uploading, the progress may appear to hang near 99%, this is normal as the file is being pre-processed.</div>
    <g:hasErrors bean="${objectModelInstance}">
        <ul class="errors" role="alert">
            <g:eachError bean="${objectModelInstance}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </g:hasErrors>
    <fieldset class="form">
        <g:render template="form"/>
    </fieldset>
    <g:uploadForm action="save">
        <fieldset class="buttons">
            <g:hiddenField id="modelTypeInput" name="modelTypeInput" value=""/>
            <g:hiddenField id="modelNameInput" name="modelNameInput" value=""/>
            <g:submitButton id="createButton" onclick="saveHiddenFields();" name="create" class="save"
                            value="${message(code: 'default.button.create.label', default: 'Create')}"/>
        </fieldset>
    </g:uploadForm>
    <div id="progressBar"/>
</div>
</body>
</html>
