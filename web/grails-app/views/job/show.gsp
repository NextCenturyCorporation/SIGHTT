<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'job.label', default: 'Job')}${jobInstance?.id}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
    <r:require module="jobShow"/>
</head>

<body>
<a href="#show-job" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                          default="Skip to content&hellip;"/></a>

<div id="show-job" class="content scaffold-show" role="main">

    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list job">

        <g:if test="${jobInstance?.jobName}">
            <li class="fieldcontain">
                <span id="name-label" class="property-label"><g:message code="job.name.label"
                                                                        default="Job Name"/></span>

                <span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${jobInstance}"
                                                                                        field="jobName"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.permissions}">
            <li class="fieldcontain">
                <span id="permissions-label" class="property-label"><g:message code="job.permissions.label"
                                                                               default="Privacy"/></span>

                <span class="property-value" aria-labelledby="permissions-label"><g:fieldValue bean="${jobInstance}"
                                                                                               field="permissions"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.owner}">
            <li class="fieldcontain">
                <span id="user-label" class="property-label"><g:message code="job.owner.label" default="Owner"/></span>

                <span class="property-value" aria-labelledby="user-label">${jobInstance.owner.username}</span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.config.backgrounds}">
            <li class="fieldcontain">
                <span id="backgrounds-label" class="property-label"><g:message code="job.config.backgrounds.label"
                                                                               default="Background(s)"/></span>

                <g:each in="${jobInstance.config.backgrounds}" status="idx" var="b">
                    <span class="property-value" aria-labelledby="backgrounds-label">
                        <g:link controller="background" action="show" id="${b.id}">
                            <div id="backThumbnail${idx}">${b.name}<r:img uri="/images/spinner.gif"/></div>
                        </g:link>
                    </span>
                </g:each>

            </li>
        </g:if>

        <g:if test="${jobInstance?.config.objectModels}">
            <li class="fieldcontain">
                <span id="objectModels-label" class="property-label"><g:message code="job.config.objectModels.label"
                                                                                default="Object Model"/></span>

                <g:each in="${jobInstance.config.objectModels}" var="o">
                    <span class="property-value" aria-labelledby="objectModels-label"><g:link controller="objectModel"
                                                                                              action="show"
                                                                                              id="${o.id}"><g:link
                                controller="objectModel" action="show" id="${o.id}"><div
                                    id="modelThumbnail">${o.name}<r:img
                                        uri="/images/spinner.gif"/></div></g:link></g:link></span>
                </g:each>

            </li>
        </g:if>

        <g:if test="${jobInstance?.config.numImages}">
            <li class="fieldcontain">
                <span id="numImages-label" class="property-label"><g:message code="job.config.numImages.label"
                                                                             default="Number of Images"/></span>

                <span class="property-value" aria-labelledby="numImages-label"><g:fieldValue bean="${jobInstance}"
                                                                                             field="config.numImages"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.config.position}">
            <li class="fieldcontain">
                <span id="position-label" class="property-label"><g:message code="job.config.position.label"
                                                                            default="Position"/></span>

                <span class="property-value" aria-labelledby="user-label"><g:fieldValue bean="${jobInstance}"
                                                                                        field="config.position"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.submitDate}">
            <li class="fieldcontain">
                <span id="submitDate-label" class="property-label"><g:message code="job.submitDate.label"
                                                                              default="Submit Date"/></span>

                <span class="property-value" aria-labelledby="submitDate-label"><g:formatDate
                        date="${jobInstance?.submitDate}"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.actualEndDate}">
            <li class="fieldcontain">
                <span id="actualEndDate-label" class="property-label"><g:message code="job.actualEndDate.label"
                                                                                 default="Actual End Date"/></span>

                <span class="property-value" aria-labelledby="actualEndDate-label"><g:formatDate
                        date="${jobInstance?.actualEndDate}"/></span>

            </li>
        </g:if>

        <g:if test="${jobInstance?.expectedEndDate}">
            <li class="fieldcontain">
                <span id="expectedEndDate-label" class="property-label"><g:message code="job.expectedEndDate.label"
                                                                                   default="Expected End Date"/></span>

                <span class="property-value" aria-labelledby="expectedEndDate-label"><g:formatDate
                        date="${jobInstance?.expectedEndDate}"/></span>

            </li>
        </g:if>

        <li class="fieldcontain">
            <span id="taskProgress-label" class="property-label">
                <g:message code="job.taskProgress.label" default="Task Progress"/>
            </span>
            <span class="property-value" aria-labelledby="taskProgress-label">
                <div id="taskProgress">
                    <div id="progressMeter">
                        <g:if test="${jobInstance.numTasks == 0}">
                            <p>Please wait while your job is initialized <r:img uri="/images/spinner.gif"/></p>
                        </g:if>
                        <g:elseif test="${jobInstance.numComplete == 0}">
                            <p>Initialized.  Waiting for an available worker... <r:img uri="/images/spinner.gif"/></p>
                        </g:elseif>
                        <g:else>
                            <g:render template="progress" bean="${jobInstance}"/>
                        </g:else>
                    </div>

                    <div id="previews">
                        <g:if test="${jobPreviewList.size > 0}">
                            <p>Sample results (refresh to display different results):</p>
                            <table style="list-style: none;">
                                <!-- Wait for tasks to be generated, then do this... -->
                                <g:each in="${jobPreviewList}">
                                    <g:render template="tasklist" bean="${it}"/>
                                </g:each>
                            </table>
                        </g:if>
                        <g:else>
                            <p>Refresh to display sample results.</p>
                        </g:else>
                    </div>
                    <g:if test="${jobHasErrors}">
                        <div id="errors">
                            <p>One or more results failed; see the job errors page.</p>
                        </div>
                    </g:if>
            </span>
        </li>
    </ol>

    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${jobInstance?.id}"/>
            <g:if test="${jobHasErrors}">
                <g:actionSubmit class="list" action="errors" value="Job Errors"/>
            </g:if>
            <g:if test="${jobInstance.numTasks > 0 && jobInstance.numComplete == jobInstance.numTasks}">
                <g:actionSubmit class="delete" action="delete" value="Delete Job"
                                onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
            </g:if>
            <g:actionSubmit class="create" action="cloneJob" value="Clone Job"/>
        </fieldset>
    </g:form>
</div>
<r:script>
          <!-- This script receives all of the events from the server that are destined for the browser. -->
          <g:each in="${jobInstance.config.backgrounds}" status="idx" var="b">
    loadThumbnail("backThumbnail${idx}","<g:createLink controller="background" action="thumbImgSrc" id="${b.id}"/>");
</g:each>
    <g:each in="${jobInstance.config.objectModels}" var="o">
        loadThumbnail("modelThumbnail","<g:createLink controller="objectModel" action="thumbImgSrc" id="${o.id}"/>");
    </g:each>
    startListener("${resource(dir: '/atmosphere/task/updateTask')}${jobInstance.id}");
</r:script>
</body>
</html>
