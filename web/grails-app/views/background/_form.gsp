<div class="fieldcontain">
    <label for="backgroundFile">
        <g:message code="background.backgroundFile.label" default="Select a file to upload"/>
    </label>
    <input type="file" name="backgroundFile"/>
</div>

<div class="fieldcontain ${hasErrors(bean: backgroundInstance, field: 'name', 'error')} ">
    <label for="name">
        <g:message code="background.name.label" default="Name"/>

    </label>
    <g:textField name="name" value="${backgroundInstance?.name}"/>
</div>

