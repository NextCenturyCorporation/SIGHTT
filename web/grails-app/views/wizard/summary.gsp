<%@ page import="com.ncc.sightt.auth.Permissions" %>
<g:applyLayout name="wizard">
    <head>
        <title>Summary</title>
        <r:require module="wizardSummary"/>
    </head>

    <body>

    <content tag="step">
        <div class="step">
            <span>Step 1</span>
            Background
        </div>

        <div class="step">
            <span>Step 2</span>
            3D Model
        </div>

        <div class="step">
            <span>Step 3</span>
            Location
        </div>

        <div class="step">
            <span>Step 4</span>
            Complexity of Perspectives
        </div>

        <div class="final step active">
            <span>Step 5</span>
            Summary
        </div>
    </content>

    <content tag="description">
        The summary of your job <b>${jobName}</b> is below. Click Launch to start your job.
    </content>

    <content tag="summary">
        <div id="summaryData">
            <ul>
                <li><span class="summary-label">Job Name</span><span class="summary-value">${jobName}</span></li>
                <g:each in="${backgrounds}" status="idx" var="background">
                    <li><span class="summary-label">Background ${idx + 1}</span><span class="summary-value"><div
                            id="backThumbnail${background.id}"><r:img uri="/images/spinner.gif"/></div></span></li>
                </g:each>
                <li><span class="summary-label">Model</span><span class="summary-value"><div
                        id="modelThumbnail${model.id}"><r:img uri="/images/spinner.gif"/></div></span></li>
                <li><span class="summary-label">Model Size</span><span
                        class="summary-value">${(Double.parseDouble(scaleData) * 100.0).trunc(1)}%</span></li>
                <li><span class="summary-label">Model Position</span><span class="summary-value">${position}</span></li>
                <li><span class="summary-label">3D Orientation Increments</span><span
                        class="summary-value">${degreeSpacing}°</span></li>
                <li><span class="summary-label">Number of Images</span><span class="summary-value">${numImages}</span>
                </li>
                <li><span class="summary-label">Orientation Distribution</span><span
                        class="summary-value">${orientationType}</span></li>
                <li><span class="summary-label">Image File Type</span><span class="summary-value">${imageType}</span>
                </li>
                <li><span class="summary-label">Image Masks</span><span
                        class="summary-value">${generateMasksString}</span></li>
                <li><span class="summary-label">Job Visibility</span><span
                        class="summary-value">${jobPrivacy}</span></li>
                <li><span class="summary-label">LightingModel</span><span
                        class="summary-value"><g:if test="${lightingModel}">
                        Elevation: ${lightingModel?.phi}<br/>
                        Azimuth: ${lightingModel?.theta}<br/>
                        Directional Intensity: ${lightingModel?.intensity}<br/>
                        Ambient Intensity: ${lightingModel?.ambient}
                    </g:if>
                    <g:else>
                        Unchanged
                    </g:else></span></li>
                <li><span class="summary-label">GroundPlane</span><span
                        class="summary-value"><g:if test="${groundPlaneModel}">
                        Position (x, y, z): [${groundPlaneModel.position.x}, ${groundPlaneModel.position.y}, ${groundPlaneModel.position.z}]<br/>
                        Rotation (x, y): [${groundPlaneModel.rotation.x}, ${groundPlaneModel.rotation.y}]
                </g:if><g:else>Unset</g:else></span></li>
            </ul>
        </div>

    </content>

    <content tag="nav">
        <g:form>

            <g:actionSubmit name="next" onclick="doStartJob();" action="startJob" class="save"
                            value="Launch"/></td>

        </g:form>
    </content>

    <r:script>
        <g:each in="${backgrounds}" var="background">
            loadThumbnail("backThumbnail${background.id}","<g:createLink controller="background" action="thumbImgSrc"
                                                                         id="${background.id}"/>");
        </g:each>
        loadThumbnail("modelThumbnail${model.id}","<g:createLink controller="objectModel" action="thumbImgSrc"
                                                                 id="${model.id}"/>");
    </r:script>

    </body>
</g:applyLayout>
