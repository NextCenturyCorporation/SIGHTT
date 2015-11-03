<%@ page import="grails.converters.JSON" %>
<g:applyLayout name="wizard_advanced">
<%--
  Created by IntelliJ IDEA.
  User: andy
  Date: 12/9/13
  Time: 3:24 PM
--%>
    <head>
        <title>Lighting Model</title>
        <r:require module="lightingcontrols"/>

        <r:script>

            var container = $('#modelview').get(0);
            $('#modelview').css('background-image','url("${backgroundURL}")');
            $('#modelview').css('background-size','cover');

            var modelViewConfig = new SceneConfig();
            modelViewConfig.rotationType="MODEL";

            var modelView = new ModelView("modelview", modelViewConfig);
            modelView.setSrcPath("${createLink(action: 'index', controller: 'main', absolute: true)}js/lib/modelview");
            modelView.loadModel("${modelLocation}");

            initializeLightingControls(modelView);

            function saveLightingModel() {
                var model = {
                    'theta': $('#azimuthSlider').slider("value"),
                    'phi': $('#elevationSlider').slider("value"),
                    'intensity': $("#intensityD").slider("value"),
                    'ambient': $("#intensityA").slider("value")
                };
                $('#lightingModel').val(JSON.stringify(model));
                return true;
            }

        </r:script>
    </head>

    <content tag="step">
        <div class="step">
            <span>Step 1</span>
            Background
        </div>

        <div class="step active">
            <span>Step 2b</span>
            Lighting Model
        </div>

        <div class="step">
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

    <content tag="description">
        Click and drag to rotate the model.  Adjust the lights to make the model appropriately
        lighted and the shadows fall in the direction from the background image.  The plane
        will not be shown in the final render.
    </content>

    <content tag="cameraLoading">
        <div id="cameraprogress"></div>
    </content>

    <content tag="controls">
        <div id="nav-description" class="message" role="status">
            Use the sliders below to change the lighting.
        </div>

        <div class="fieldcontain">
            Elevation: <span id="yval">0</span>
            <div id="elevationSlider"></div>

            Azimuth: <span id="xval">0</span>
            <div id="azimuthSlider"></div>

            Directional Intensity: <span id="dval">0</span>

            <div id="intensityD"></div>

            Ambient Intensity: <span id="aval">0</span>

            <div id="intensityA"></div>
        </div>
    </content>

    <content tag="nav">
        <g:form name="activeCameras" action="setLightingModel" onsubmit="return saveLightingModel()">
            <g:hiddenField id="lightingModel" name="lightingModel"></g:hiddenField>
            <g:submitButton value="Continue" class="save wizard-next-button" name="next"></g:submitButton>
        </g:form>
    </content>

</g:applyLayout>