<%@ page import="grails.converters.JSON" %>
<g:applyLayout name="wizard_advanced">

<%--
  Created by IntelliJ IDEA.
  User: andy
  Date: 12/9/13
  Time: 3:24 PM
--%>

<head>
    <title>Limit Model Aspects</title>
    <r:require module="limitedaspects"/>
    <r:script>
        var modelView;
        var aspectHandler;
        var doStats = true;
        var numRotations;


        window.onload = function() {
            //Prevent buttons from bouncing to top of page on click
            $( 'a[href="#"]' ).click( function(e) {
                e.preventDefault();
            } );
            var sceneConfig = new SceneConfig();
            sceneConfig["showShadows"]=false;
            modelView = new ModelView("modelview",sceneConfig);
            modelView.setSrcPath("${createLink(action: 'index', controller: 'main', absolute: true)}js/lib/modelview");
            modelView.loadModel("${modelLocation}");
            aspectHandler = new AspectHandler();
            //Load cameras asynchronously!
            var camListUrl = "${createLink(action: 'cameraList', absolute: true)}";

            $.post(camListUrl, function(data,textStatus,jqXHR) {
                var numTessella = 0;
                var maxNeighborLevels = 0;
                if(data.error) {
                    doStats=false;
                    numRotations = 0;
                    updateStats();
                } else {
                    aspectHandler.init(modelView,data);
                    numRotations = data.rawResults.rotations;
                    updateStats();
                    numTessella = data.tessella.length;
                    maxNeighborLevels = Math.max(1.0, (Math.log(numTessella)/Math.log(6)));
                }
                $("#cameraprogress").hide();
                var maxNeighborLevelsRounded = Math.round(maxNeighborLevels);
                $('#neighborlevels').slider({min: 0,max:maxNeighborLevelsRounded,step:1,slide: function(event,ui){updateNeighborLevels(event,ui);}});
            });
            var aspects = aspectHandler.aspects;

            $(document).on("modified-aspects",function(event){
            updateStats();
            });

        }

        function updateStats() {
            var numActive = 0;
            if(doStats) {
                for(var elem in aspectHandler.aspects){
                    if(aspectHandler.aspects[elem].active==true){
                        numActive++;
                    }
                }
            }
            $("#active-aspects").text(numActive);
            $("#numberof-images").text(numActive*numRotations);
            $("#numberof-rotations").text(numRotations);
        }

        function saveLimitedAspectsData() {
            var camList = $.map(aspectHandler.aspects,function(value,index) {
                return {"active":value.active}
            });
            var customNumberOfImages = $("#override").prop("checked")?$("#overrideNumImages").val():-1;
            var doRandomize = $("#randomize").prop("checked");
            var limitedAspectsData = {
                'camList': camList,
                'customNumberOfImages': customNumberOfImages,
                'randomize': doRandomize
            }
            var data = JSON.stringify(limitedAspectsData);
	    // alert(data);
            $("#limitedAspectsData").val(data);
        }

        function updateNeighborLevels(event,ui){
            var newVal = ui.value;
            $("#neighbors").text(newVal);
            aspectHandler.neighborLevel = ui.value;
            aspectHandler.loadFaceGroupings();
        }

        $("#overrideNumImages").hide();
        $('#override').change(function() {

            if($(this).prop("checked")) {
                $("#overrideNumImages").show();
            } else {
                $("#overrideNumImages").hide();
            }

        });

        var xOffset = -40;
        var yOffset = 10;
        $(".tooltip").hover(function(e){

            this.t = this.title;
            this.title = "";
            $("body").append("<p id='tooltip'>"+this.t+"</p>");
            $("#tooltip").css("top",(e.pageY - xOffset) + "px")
                        .css("left",(e.pageX + yOffset) + "px")
                        .fadeIn("fast");
        }, function() {
            this.title = this.t;
            $("#tooltip").remove();
        });

        $(".tooltip").mousemove(function(e){
		$("#tooltip")
			.css("top",(e.pageY - xOffset) + "px")
			.css("left",(e.pageX + yOffset) + "px");
	});

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

    <div class="step active">
        <span>Step 4b</span>
        Select Limited Aspects
    </div>

    <div class="final step">
        <span>Step 5</span>
        Summary
    </div>
</content>

<content tag="description">
    Click and drag to rotate the view.  Click cameras to enable/disable view aspects. &nbsp;
    Green = enabled, Red = disabled
</content>

<content tag="cameraLoading">
    <div id="cameraprogress">Loading Cameras <r:img uri="/images/spinner.gif"/></div>
</content>

<content tag="afterModelview">
    <div class="status-block">
        <div>
            <span id="active-aspects" class="bound-data"><r:img uri="/images/spinner.gif"/></span>
            <label for="active-aspects" class="bound-data-label">active aspects.</label>
        </div>

        <div>
            <span id="numberof-rotations" class="bound-data"><r:img uri="/images/spinner.gif"/></span>
            <label class="bound-data-label">rotations per aspect.</label>
        </div>

        <div>
            <span id="numberof-images" class="bound-data"><r:img uri="/images/spinner.gif"/></span>
            <label for="numberof-images" class="bound-data-label">images produced.</label>
        </div>

    </div>
</content>

<content tag="controls">
    <div id="faceselectors" class="ui-block">
        <div id="nav-description" class="message" role="status">
            Use the checkboxes below to toggle different sides of the model on/off.
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Front:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('front', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('front', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Back:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('back', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('back', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Left:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('left', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('left', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Right:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('right', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('right', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Top:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('top', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('top', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label">Bottom:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('bottom', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('bottom', false);"></a>
                </li>
            </ul>
        </div>

        <div class="faceswitch-block">
            <span class="faceswitch-label faceswitch-all">All:</span>
            <ul class="facebuttons">
                <li><a href="#" class="icon-on" onclick="aspectHandler.toggleFaces('all', true);"></a></li>
                <li><a href="#" class="icon-off" onclick="aspectHandler.toggleFaces('all', false);"></a>
                </li>
            </ul>
        </div>

        <div class="slider-block">
            <span class="slider-label">Neighbor Depth:</span><span id="neighbors">0</span>

            <div id="neighborlevels" class="neighbor-slider"></div>

            <div class="message neighbor-desc"
                 role="status">Depth 0: single face<br/>Depth 1: face and 1-hop neighbors<br/>Depth 2: face and 2-hop neighbors<br/>etc.
            </div>

        </div>

        <div class="slider-block">
            <div class="tooltip"
                 title="Choose a specific number of images you want and let SIGHTT distribute them evenly amongst the selected aspects."><label
                    for="override">Override total number of images</label><g:checkBox name="override"></g:checkBox>
            </div>
            <input id="overrideNumImages" type="text" pattern="[0-9]*" name="overrideNumImages"
                   placeholder="Number of Images"/>

            <div class="tooltip"
                 title="Randomize the initial placement for the aspects. If you select this your job will not be reproducible!"><label
                    for="randomize">Randomize Job</label><g:checkBox name="randomize"></g:checkBox></div>
        </div>

</content>


<content tag="nav">
    <g:form name="activeCameras" action="setActiveCameras">
        <g:hiddenField name="limitedAspectsData"></g:hiddenField>
        <g:submitButton name="setActiveCamerasButton" class="save wizard-next-button" value="Continue" onclick="saveLimitedAspectsData()"/>
    </g:form>
</content>

</g:applyLayout>
