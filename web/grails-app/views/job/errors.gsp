<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'job.label', default: 'Job')}${jobId}"/>
    <r:require module="thumbnail"/>
    <title><g:message code="default.show.errors.label" args="[entityName]"/></title>
</head>

<body>
<a href="#show-job" class="skip" tabindex="-1"><g:message code="default.link.skip.label"
                                                          default="Skip to content&hellip;"/></a>

<div class="nav" role="navigation">
    <ul>
        <li><g:link class="list" action="show" id="${jobId}"><g:message code="default.show.label"
                                                                        args="[entityName]"/></g:link></li>
    </ul>
</div>

<p>Failed results:</p>

<div id="errors">
    <table style="list-style: none;">
        <tr>
            <td style="vertical-align: middle"><b>ID</b></td>
            <td style="vertical-align: middle"><b>X</b></td>
            <td style="vertical-align: middle"><b>Y</b></td>
            <td style="vertical-align: middle"><b>Z</b></td>
            <td style="vertical-align: middle"><b>Error</b></td>
            <td style="vertical-align: middle"><b>Output</b></td>
        </tr>
        <g:each in="${failedJobList}">
            <tr id="task${it.id}">
                <td style="vertical-align: middle">${it.taskNumber}</td>
                <td style="vertical-align: middle">${it.xrot}</td>
                <td style="vertical-align: middle">${it.yrot}</td>
                <td style="vertical-align: middle">${it.zrot}</td>
                <td style="vertical-align: middle">${it.error}</td>
                <td style="vertical-align: middle"><div class="information"
                                                        onclick='displayOutputPopup("${it.stdout}", "${it.stderr}", "${it.exitValue}");'></div>
                </td>
            </tr>
        </g:each>
    </table>
</div>
<r:script>
    function displayOutputPopup(stdout, stderr, exitValue) {
        alert("<b>Standard Output:</b>\n" + stdout + "\nStandard Error:\n" + stderr +
                "\nExit Value:  " + exitValue);
    }
</r:script>
</body>
</html>
