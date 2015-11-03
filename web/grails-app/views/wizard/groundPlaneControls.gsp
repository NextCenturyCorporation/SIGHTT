<g:applyLayout name="wizard">

    <head>
        <title>Describe Ground Plane</title>
        <r:require module="groundPlaneControls"/>

        <r:script>

            var container = $('#canvasDiv').get(0);
            $('#canvasDiv').css('background-image','url("${backgroundURL}")');
            $('#canvasDiv').css('background-size','cover');

            var groundControls = initializeGroundControls(container, ${backgroundWidth}, ${backgroundHeight});
            container.appendChild(groundControls.getRenderer().domElement);
            groundControls.animate();

            /**
            * Rounds all floats to 2 decimal places.
            * @returns {boolean}
            */
            function saveData() {
            var scaleMultiplier = Number((${backgroundWidth}/WIDTH).toFixed(2));
            var groundPlaneData = {
                scaleMultiplier: scaleMultiplier,
                position: {
                    x: Number((groundControls.group.position.x*scaleMultiplier).toFixed(2)),
                    y: Number((groundControls.group.position.y*scaleMultiplier).toFixed(2)),
                    z: Number((groundControls.group.position.z*scaleMultiplier).toFixed(2))
                },
                rotation: {
                    x: Number((groundControls.group.rotation.x).toFixed(2)),
                    y: Number((groundControls.group.rotation.y).toFixed(2))
                }}
                var groundPlaneDataJSON = JSON.stringify(groundPlaneData);
                // DEBUG statement, see what the data is being passed back
                // alert(groundPlaneDataJSON);
                $("#groundPlaneData").val(groundPlaneDataJSON);
                return true;
            }

        </r:script>
    </head>


    <content tag="step">
        <div class="step active">
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

        <div class="step">
            <span>Step 5</span>
            Job Settings
        </div>

        <div class="final step">
            <span>Step 6</span>
            Summary
        </div>
    </content>

    <content tag="description">
        Describe the ground plane in the image.  Click and drag mouse to rotate the grid to match the ground plane;
        use shift-click and drag to move the grid.
        <g:link controller="main" action="faq" fragment="groundplane">Click here for instructions.</g:link>
    </content>


    <content tag="background">
        <h1 class="preview">Preview</h1>

        <div id="canvasWrapper">
            <div id="canvasDiv"></div>
        </div>

        <div class="fieldcontain">

            Grid Size : <span id="gridSizeVal">0</span>

            <div id="sliderGridSize"></div>

        </div>
    </content>


    <content tag="left">

    </content>


    <content tag="nav">
        <g:form name="nav" action="finishGroundPlaneControls" onsubmit="saveData()">
            <g:hiddenField id="groundPlaneData" name="groundPlaneData"></g:hiddenField>
            <g:submitButton name="next" value="Continue" class=" wizard-next-button"/>
        </g:form>
    </content>

</g:applyLayout>