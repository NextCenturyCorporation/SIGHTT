<g:applyLayout name="wizard">

    <head>
        <title>Select Background</title>
        <r:require module="wizardBackground"/>
        <r:script>
            selectedBackgrounds = [];
            selectedBackgroundReferences = {};
            backgroundUrls = {};
            backgroundLinkAddr = "<g:createLink action='getBackgroundUrl' absolute='true'/>";

            $("#uploadPreview").hide();
            $("#uploadText").hide();
            createBackgroundDropzone("${createLink(controller: 'background', action: 'save')}");

            $(function(){
	        $("#carousel").slick({
			vertical: true,
			slidesToShow: 5,
			infinite: true
		});
            });

	    $('#ui-carousel-next').click(function(){
	        $("#carousel").slickNext();
	        event.preventDefault();   /* This keeps page from reloading / scrolling up */
	    });

	    $('#ui-carousel-prev').click(function(){
	    	$("#carousel").slickPrev();
	    	event.preventDefault();   /* This keeps page from reloading / scrolling up */
	    });

            <g:each in="${preSelectedBackgrounds}" var="background">
                displayedBackground = ${background as grails.converters.JSON};
               addBackground();
            </g:each>

            <g:if test="${uploadMessage}">
                changeUploadText("${uploadMessage}", "${messageType}");
            </g:if>
        </r:script>
    </head>

    <content tag="step">
        <div class="step active">
            <span>Step 1</span>
            Backgrounds
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

    <content tag="description"> Select an existing background image,
        or upload one of your own by dragging and dropping an image
        into the preview area.  Click 'Continue' to add a model; or
        'Advanced' to define a ground plane for shadows.  </content>

    <content tag="left">
        <h1>Select existing</h1>

        <div id="carousel-container">
            <a href="javascript:void(0)" id="ui-carousel-prev"></a> 

            <div id="carousel">
                <g:each in="${backgroundList}" var="background">

                    <div><img class="carousel-centered" src="${backgroundThumbnailSrcMap[background.id]}"
                              title="${background.name}"
                              onclick='selectBackgroundFromCarousel(${background as grails.converters.JSON});'/>
                    </div>
                </g:each>
            </div>

            <a href="javascript:void(0)" id="ui-carousel-next"></a>
        </div>

        <label for="selectedBackgroundsList">Selected Background:</label>

        <div id="selectedBackgroundsList"></div>

    </content>

    <content tag="upload">
        <h1>Upload new</h1>
        <button id="uploadButton">Browse...</button>

        <div id="uploadPreview"></div>
    </content>

    <content tag="background">
        <h1 class="preview">Preview Filename:</h1> <span class="preview" id="preview-filename">None</span>

        <div id="backImage">
            <div id="previewText" class="errors">
                Please select a background image from the left or upload one of your own above.
            </div>

        </div>
        %{--
        <div id="previewText" class="errors">
           Please select a background image.
        </div>
        --}%

        <div id="uploadText" class="message"></div>
    </content>

    <content tag="nav">
        <g:form name="nav" action="finishSelectBackground">
            <g:hiddenField id="backgroundIds" name="backgroundIds" value=""/>
            <g:submitButton name="next" class="sightt-disabled wizard-next-button" value="Continue" disabled="disabled"
                            onclick='setBackgroundIdsFromList()'/>
            <g:submitButton name="next" class="sightt-disabled wizard-next-button" value="Advanced" disabled="disabled"
                            onclick='setBackgroundIdsFromList()'/>
        </g:form>
    </content>

</g:applyLayout>

