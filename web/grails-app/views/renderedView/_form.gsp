<div class="fieldcontain">
    <label for="renderedImage">
        <g:message code="renderedView.renderedImage.label" default="Rendered Image"/>
    </label>
    <input name="renderedImage" type="file"/>
</div>

<div class="fieldcontain ${hasErrors(bean: renderedViewInstance, field: 'name', 'error')} ">
    <label for="name">
        <g:message code="renderedView.name.label" default="Name"/>

    </label>
    <g:textField name="name" value="${renderedViewInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: renderedViewInstance, field: 'sizeInMeters', 'error')} ">
    <label for="sizeInMeters">
        <g:message code="renderedView.sizeInMeters.label" default="Size (m)"/>

    </label>
    <g:textField name="sizeInMeters" value="${renderedViewInstance?.sizeInMeters}"/>
</div>

