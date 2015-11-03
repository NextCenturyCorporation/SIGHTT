<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'background', 'error')} required">
    <label for="background">
        <g:message code="jobTask.background.label" default="Background"/>
        <span class="required-indicator">*</span>
    </label>
    <g:select id="background" name="background.id" from="${com.ncc.sightt.Background.list()}" optionKey="id" required=""
              value="${jobTaskInstance?.background?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'completed', 'error')} ">
    <label for="completed">
        <g:message code="jobTask.completed.label" default="Completed"/>

    </label>
    <g:checkBox name="completed" value="${jobTaskInstance?.completed}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'completedTime', 'error')} required">
    <label for="completedTime">
        <g:message code="jobTask.completedTime.label" default="Completed Time"/>
        <span class="required-indicator">*</span>
    </label>
    <g:datePicker name="completedTime" precision="day" value="${jobTaskInstance?.completedTime}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'compositeImage', 'error')} required">
    <label for="compositeImage">
        <g:message code="jobTask.compositeImage.label" default="Composite Image"/>
        <span class="required-indicator">*</span>
    </label>
    <g:select id="compositeImage" name="compositeImage.id" from="${com.ncc.sightt.CompositeImage.list()}" optionKey="id"
              required="" value="${jobTaskInstance?.compositeImage?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'renderedView', 'error')} required">
    <label for="renderedView">
        <g:message code="jobTask.renderedView.label" default="Rendered View"/>
        <span class="required-indicator">*</span>
    </label>
    <g:select id="renderedView" name="renderedView.id" from="${com.ncc.sightt.RenderedView.list()}" optionKey="id"
              required="" value="${jobTaskInstance?.renderedView?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: jobTaskInstance, field: 'startTime', 'error')} required">
    <label for="startTime">
        <g:message code="jobTask.startTime.label" default="Start Time"/>
        <span class="required-indicator">*</span>
    </label>
    <g:datePicker name="startTime" precision="day" value="${jobTaskInstance?.startTime}"/>
</div>

