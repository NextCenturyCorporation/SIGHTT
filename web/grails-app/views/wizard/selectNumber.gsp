<g:applyLayout name="wizard">
    <head>
        <title>Select Complexity of Perspectives</title>
        <r:require module="wizardNumber"/>
        <r:script>
      $(document).ready(function() {
        loadCanvas("${scaleData}", "<sightt:wizardLink path="${background.filePath}"/>", "<sightt:wizardLink
                path="${model.imageFilePath}"/>");
        $('#selNum').attr('checked',true);
        $('#selRot').attr('checked',false);
        $('#chooseByRotation').show();
        $('#chooseByNumber').hide();
      });
      window.onload = function() {
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
            3D Models
        </div>

        <div class="step">
            <span>Step 3</span>
            Location
        </div>

        <div class="step active">
            <span>Step 4</span>
            Complexity of Perspectives
        </div>

        <div class="step">
            <span>Step 5</span>
            Job Settings
        </div>

        <div class="final step">
            <span>Step 6</span>
            Summary
        </div>
    </content>

    <content tag="background">
        <h1 class="preview">Preview</h1>

        <div id="canvasWrapper" class="fieldcontain">
            <div id="canvas"></div>
        </div>

    </content>

    <content tag="description"> Select the number of images to be
        generated.  The model will be rotated to produce that many
        images.  Click 'Advanced' for more control over which
        rotations are produced.  
    </content>

    <content tag="left">
        <h1>Select Options</h1>

        <form name="numberForm">
            <p>3D Orientation Increments</p>
            <g:if test="${flash.message}">
                <div id="numImagesMessage" class="message" role="status"><g:message code="${flash.message}"
                                                                                    args="${flash.args}"/></div>
            </g:if>
            <div id="chooseByRotation" class="indented">
                <g:radioGroup name="rotGroup" labels="${rotationLabels}"
                              values="${rotationValues}"
                              value="${rotationDefault}"
                              onclick="hideElement('numImagesMessage');">
                    <p>${it.radio} <g:message code="${it.label}"/></p>
                </g:radioGroup>
            </div>
        </form>
    </content>

    <content tag="nav">
        <!--
hiddenFields without a value are set from THIS page, otherwise their values are passed in from previous pages in the wizard process
 -->
        <g:form name="nav" action="finishSelectNumber" onsubmit="return storeSelectNumberInfo();">
            <g:hiddenField id="genMethod" name="genMethod" value="rot"/>
            <g:hiddenField id="reproducible" name="reproducible" value="true"/>
            <g:hiddenField id="spacing" name="spacing" value=""/>
            <g:submitButton name="next" class="save wizard-next-button" value="Continue"/>
            <g:submitButton name="next" class="save wizard-next-button" value="Advanced"/>
        </g:form>
    </content>

</g:applyLayout>

