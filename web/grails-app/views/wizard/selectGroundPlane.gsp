<g:applyLayout name="wizard">

    <head>
        <title>Select Location</title>
        <r:require module="wizardLocation"/>
        <r:script>

            $("input[name='locationGroup']").change(function() {
                $('#position').val($(this).val());
            });

            $(document).ready(function() {
                // On loading, set the location and the points if they are set
                $("#position").val("${locationDefault}");
                setPointsFromJSON("${points}");

                loadCanvas( "${scaleData}", "<sightt:wizardLink path="${background.filePath}"/>", "<sightt:wizardLink
                path="${model.imageFilePath}"/>",canvasInitializedCallback);
            });

            <g:if test="${buttonDisabled}">
                disableButton();
            </g:if>

            function setPoints()
            {
                var pointsJSONString = getPointsJSON();
                $('#points').val(pointsJSONString);
            }

        </r:script>
    </head>

    <content tag="step">
        <div class="step">
            <span>Step 1</span>
            Background
        </div>

        <div class="step">
            <span>Step 2</span>
            3D Model
        </div>

        <div class="step active">
            <span>Step 3</span>
            Location
        </div>

        <div class="step">
            <span>Step 4</span>
            Complexity of Perspectives
        </div>

        <div class="final step">
            <span>Step 5</span>
            Summary
        </div>
    </content>

    <content tag="background">
        <h1 class="preview">Preview</h1>

        <div id="canvasWrapper">
            <div id="canvas"></div>
        </div>
    </content>

    <content tag="description">
        Select the location of the model in the background.  Click in the image for point, line, or area.
        <g:link controller="main" action="faq" fragment="locations">Click here for instructions.</g:link>
    </content>

    <content tag="left">
        <h1>Select location</h1>

        <form name="locationForm">
            <g:radioGroup name="locationGroup" labels="${com.ncc.sightt.ModelLocation.values() as String[]}"
                          values="${com.ncc.sightt.ModelLocation.values() as String[]}" value="${locationDefault}"
                          onclick="selectLocationButtonHandler()">
                <p>
                    ${it.radio} <g:message code="${it.label}"/>
                </p>
            </g:radioGroup>
        </form>

        <button onclick="clearPoints()">Clear</button>

        <p/>

        <g:textArea class="locationdesc" name="locationDesc" id="locationDesc" readonly="readonly"></g:textArea>
    </content>

    <content tag="nav">
        <g:form name="nav">
            <g:hiddenField id="position" name="position" value=""/>
            <g:hiddenField id="points" name="points" value=""/>
            <g:actionSubmit id="next" name="next" class=" wizard-next-button"
                            value="Continue" onclick="setPoints()"
                            action="finishSelectLocation"/>
        </g:form>
    </content>

</g:applyLayout>

