<div
        class="fieldcontain ${hasErrors(bean: jobInstance, field: 'jobName', 'error')} ">
    <label for="jobName"><g:message code="job.jobName.label"
                                    default="Give this job a name"/>

    </label>
    <g:textField name="jobName" value="${jobInstance?.jobName}"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: jobInstance, field: 'config.numImages', 'error')} required">
    <label for="numImages"><g:message code="job.config.numImages.label"
                                      default="Num Images"/> <span class="required-indicator">*</span>
    </label>
    <g:field name="numImages" type="number"
             value="${jobInstance.config.numImages}" required=""/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: jobInstance, field: 'config.objectModels', 'error')} ">
    <label for="objectModels"><g:message
            code="job.config.objectModels.label" default="Object Models"/>

    </label>
    <g:select name="objectModels"
              from="${com.ncc.sightt.ObjectModel.list()}" multiple="multiple"
              optionKey="id" size="5" optionValue="name"
              value="${jobInstance?.config.objectModels*.id}" class="many-to-many"/>
</div>

<div
        class="fieldcontain ${hasErrors(bean: jobInstance, field: 'config.backgrounds', 'error')} ">
    <label for="backgrounds"><g:message
            code="job.config.backgrounds.label" default="Backgrounds"/>

    </label>
    <g:select name="backgrounds" from="${com.ncc.sightt.Background.list()}"
              multiple="multiple" optionKey="id" size="5" optionValue="name"
              value="${jobInstance?.config.backgrounds*.id}" class="many-to-many"/>
</div>

<!-- 


<div
    class="fieldcontain ${hasErrors(bean: jobInstance, field: 'actualEndDate', 'error')} required">
    <label for="actualEndDate"> <g:message
        code="job.actualEndDate.label" default="Actual End Date"/> <span
        class="required-indicator">*</span>
    </label>
<g:datePicker name="actualEndDate" precision="day"
              value="${jobInstance?.actualEndDate}"/>
</div>

<div
    class="fieldcontain ${hasErrors(bean: jobInstance, field: 'expectedEndDate', 'error')} required">
    <label for="expectedEndDate"> <g:message
        code="job.expectedEndDate.label" default="Expected End Date"/> <span
        class="required-indicator">*</span>
    </label>
<g:datePicker name="expectedEndDate" precision="day"
              value="${jobInstance?.expectedEndDate}"/>
</div>

<div
    class="fieldcontain ${hasErrors(bean: jobInstance, field: 'submitDate', 'error')} required">
    <label for="submitDate"> <g:message code="job.submitDate.label"
                                        default="Submit Date"/> <span class="required-indicator">*</span>
    </label>
<g:datePicker name="submitDate" precision="day"
              value="${jobInstance?.submitDate}"/>
</div>

-->
