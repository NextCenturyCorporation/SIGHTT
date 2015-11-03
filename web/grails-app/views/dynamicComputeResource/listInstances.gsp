<g:applyLayout name="main">
    <head>
        <r:require module="dcrList"/>
    </head>
    <g:if test="${flash.message}">
        <div class="confirm" role="status">
            ${flash.message}
        </div>
    </g:if>
    <h1>
        The following
        <g:if test="${numInstances == 1}">instance is
        </g:if>
        <g:else>
            ${numInstances} instances are
        </g:else>
        currently processing tasks
    </h1>
    <g:def var="instanceNumber" value="0"/>
    <table id="instancesTbl">
        <tr>
            <th>Instance #</th>
            <th>Reservation Id</th>
            <th>Instance Id</th>
            <th>Image Id</th>
            <th>Instance Type</th>
            <th>Instance Name</th>
            <th>Lifecycle</th>
            <th>Terminate</th>
        </tr>
        <g:each
                in="${instances}">
            <p>

            <div>

                <tr>
                    <td>
                        ${instanceNumber++}
                    </td>
                    <td>
                        ${it.reservationId}
                    </td>
                    <td>
                        ${it.instanceId}
                    </td>
                    <td>
                        ${it.imageId}
                    </td>
                    <td>
                        ${it.instanceType}
                    </td>
                    <td>
                        ${it.name}
                    </td>
                    <td>
                        ${it.instance.state.name}
                    </td>
                    <td><g:form action="stopInstance">
                        <fieldset class="buttons">
                            <g:actionSubmit class="delete" action="stopInstance"
                                            value="Terminate" formnovalidate=""
                                            onclick="return confirm('${message(code: 'dynamic.terminate.confirm', default: 'Are you sure?', args: ["${it.instanceId}"])}');"/>
                        </fieldset>
                        <g:hiddenField name="instanceId" value="${it.instanceId}"/>
                    </g:form></td>
                </tr>

            </div>
        </g:each>
    </table>

    <p>
        <g:link action="requestInstances">Click here</g:link>
        to add instances manually
    </p>

    <p>
        <g:link action="changeSettings">Click here</g:link>
        to update settings for starting instances
    </p>

    <p>
        History of activity:
    </p>

    <div class="code">
        ${status}
    </div>
</g:applyLayout>
