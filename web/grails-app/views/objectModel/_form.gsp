<%@ page import="com.ncc.sightt.ObjectModel" %>


<div class="fieldcontain">
    <label for="uploadPreview" class="property-label">
        <g:message code="objectModel.modelFile.label" default="Model File"/>
    </label>


    <div id="uploadPreview" class="property-value"><button id="browseButton">Browse...</button></div>
    %{--<g:textField id="uploadText" name="filename" value=""/>--}%

</div>

<div class="fieldcontain ${hasErrors(bean: objectModelInstance, field: 'modelType', 'error')} required">
    <label for="modelType" class="property-label">
        <g:message code="objectModel.modelType.label" default="Model Type"/>
    </label>
    <g:select name="modelType" from="${com.ncc.sightt.ModelType?.values()}"
              keys="${com.ncc.sightt.ModelType.values()*.name()}" required=""
              value="${objectModelInstance?.modelType?.name()}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: objectModelInstance, field: 'name', 'error')} ">
    <label for="name" class="property-label">
        <g:message code="objectModel.name.label" default="Name"/>
    </label>
    <g:textField id="objectModelName" name="name" value="${objectModelInstance?.name}" pattern="[A-Za-z0-9_-]+"/>
</div>
