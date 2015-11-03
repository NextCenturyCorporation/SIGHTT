<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'jobTask.label', default: 'JobTask')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-jobTask" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                              default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
        <li><g:link class="create" action="create"><g:message code="default.new.label"
                                                              args="[entityName]"/></g:link></li>
    </ul>
</div>

<div id="show-jobTask" class="content scaffold-show" role="main">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <ol class="property-list jobTask">

        <g:if test="${jobTaskInstance?.background}">
            <li class="fieldcontain">
                <span id="background-label" class="property-label"><g:message code="jobTask.background.label"
                                                                              default="Background"/></span>

                <span class="property-value" aria-labelledby="background-label"><g:link controller="background"
                                                                                        action="show"
                                                                                        id="${jobTaskInstance?.background?.id}">${jobTaskInstance?.background?.encodeAsHTML()}</g:link></span>

            </li>
        </g:if>

        <g:if test="${jobTaskInstance?.completed}">
            <li class="fieldcontain">
                <span id="completed-label" class="property-label"><g:message code="jobTask.completed.label"
                                                                             default="Completed"/></span>

                <span class="property-value" aria-labelledby="completed-label"><g:formatBoolean
                        boolean="${jobTaskInstance?.completed}"/></span>

            </li>
        </g:if>

        <g:if test="${jobTaskInstance?.completedTime}">
            <li class="fieldcontain">
                <span id="completedTime-label" class="property-label"><g:message code="jobTask.completedTime.label"
                                                                                 default="Completed Time"/></span>

                <span class="property-value" aria-labelledby="completedTime-label"><g:formatDate
                        date="${jobTaskInstance?.completedTime}"/></span>

            </li>
        </g:if>

        <g:if test="${jobTaskInstance?.compositeImage}">
            <li class="fieldcontain">
                <span id="compositeImage-label" class="property-label"><g:message code="jobTask.compositeImage.label"
                                                                                  default="Composite Image"/></span>

                <span class="property-value" aria-labelledby="compositeImage-label"><g:link controller="compositeImage"
                                                                                            action="show"
                                                                                            id="${jobTaskInstance?.compositeImage?.id}">${jobTaskInstance?.compositeImage?.encodeAsHTML()}</g:link></span>

            </li>
        </g:if>

        <g:if test="${jobTaskInstance?.renderedView}">
            <li class="fieldcontain">
                <span id="renderedViewObject-label" class="property-label"><g:message code="jobTask.renderedView.label"
                                                                                      default="Rendered View"/></span>

                <span class="property-value" aria-labelledby="renderedView-label"><g:link controller="renderedView"
                                                                                          action="show"
                                                                                          id="${jobTaskInstance?.renderedView?.id}">${jobTaskInstance?.renderedView?.encodeAsHTML()}</g:link></span>

            </li>
        </g:if>

        <g:if test="${jobTaskInstance?.startTime}">
            <li class="fieldcontain">
                <span id="startTime-label" class="property-label"><g:message code="jobTask.startTime.label"
                                                                             default="Start Time"/></span>

                <span class="property-value" aria-labelledby="startTime-label"><g:formatDate
                        date="${jobTaskInstance?.startTime}"/></span>

            </li>
        </g:if>

    </ol>
    <g:form>
        <fieldset class="buttons">
            <g:hiddenField name="id" value="${jobTaskInstance?.id}"/>
            <g:link class="edit" action="edit" id="${jobTaskInstance?.id}"><g:message code="default.button.edit.label"
                                                                                      default="Edit"/></g:link>
            <g:actionSubmit class="delete" action="delete"
                            value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                            onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
        </fieldset>
    </g:form>
</div>
</body>
</html>
