<g:applyLayout name="wizard">

    <head>
        <title>Advanced Options</title>
        <r:require module="wizardAdvanced"/>
        <r:script>
      setupAdvancedOptions("${generateMasks}")
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
        <div class="message" role="status">
            Select advanced options for the job.
        </div>
    </content>

    <content tag="background">

        <g:form name="customNumberOfImagesForm">
            <div>
                <label for="customNumberOfImages">
                    <g:message code="customNumberOfImages.label" default="Enter custom number of images:"/>
                </label>
                <g:textField name="customNumberOfImages" value="${numImages}"/>
            </div>
        </g:form>

        <g:form name="orientationTypeForm">
            <p>Orientation Distribution:</p>

            <div id="chooseOrientationType" class="indented">
                <g:radioGroup name="orientationTypeGroup" labels="${orientationTypes}" values="${orientationTypes}"
                              value="${orientationTypeDefault}">
                    <p>${it.radio} <g:message code="${it.label}"/></p>
                </g:radioGroup>
            </div>
        </g:form>

        <g:form name="imageTypeForm">
            <p>Image File Type:</p>

            <div id="chooseImageType" class="indented">
                <g:radioGroup name="imageTypeGroup" labels="${imageTypes}" values="${imageTypes}"
                              value="${imageTypeDefault}">
                    <p>${it.radio} <g:message code="${it.label}"/></p>
                </g:radioGroup>
            </div>
        </g:form>

        <g:form name="generateMasksForm">
            <p>Generate Masks for All Layers: &nbsp <input id="generateMasksCheckbox" type="checkbox"
                                                           onclick="setGenerateMasks(document.getElementById('generateMasksCheckbox').checked);">
            </p>
        </g:form>

    </content>

    <content tag="nav">
        <g:form name="nav" action="finishSelectAdvancedOptions">
            <g:hiddenField id="numImages" name="numImages" value="${numImages}"/>
            <g:hiddenField id="imageTypeString" name="imageTypeString" value=""/>
            <g:hiddenField id="orientationTypeString" name="orientationTypeString" value=""/>
            <g:hiddenField id="generateMasksString" name="generateMasksString" value="false"/>
            <g:submitButton name="next" onclick="storeAdvancedOptions();" class="save" value="Continue"/>
        </g:form>
    </content>

</g:applyLayout>

