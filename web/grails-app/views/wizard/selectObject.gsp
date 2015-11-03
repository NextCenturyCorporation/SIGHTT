<g:applyLayout name="wizard">
    <head>
        <title>Select Model</title>
        <r:require module="wizardObject"/>
        <r:script>
        $(document).ready(function() {

             var renderedModelUrl = null;
            <g:if test="${selectedModel}">
            renderedModelUrl = "<sightt:wizardLink path="${selectedModel.imageFilePath}"/>";
            $('#modelId').val(${selectedModel.id});
        </g:if>

            // Reset this value when the page reloads
            $("#size").val("");

            loadCanvas("${selectedScale}", "<sightt:wizardLink path="${background.filePath}"/>",
                renderedModelUrl
                 );
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
        });

        // IE kludge
        var isIE = /*@cc_on!@*/false || !!document.documentMode; // At least IE6
	
        window.onload = function() {
            if (isIE) { 
                if(!window.location.hash) {
                    window.location = window.location + '#loaded';
                    window.location.reload();
                }
            }
        }
        </r:script>
    </head>

    <content tag="step">
        <div class="step">
            <span>Step 1</span>
            Backgrounds
        </div>

        <div class="step active">
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

    <content tag="left">
        <h1>Select existing</h1>

        <div id="carousel-container">
            <a href="javascript:void(0)" id="ui-carousel-prev"></a>

            <div id="carousel">
                <g:each in="${modelList}" var="model">
                    <div><img class="carousel-centered" src="${modelThumbnailSrcMap[model.id]}" title="${model.name}"
                              onclick='selectModel("${model.id}", "<sightt:wizardLink
                            path="${background.filePath}"/>", "<sightt:wizardLink path="${model.imageFilePath}"/>");'/>
                    </div>
                </g:each>
            </div>

            <a href="javascript:void(0)" id="ui-carousel-next"></a>
        </div>

    </content>

    <content tag="background">
        <h1 class="preview">Preview</h1>

        <div id="canvasWrapper">

            <label for="sizebuttons">Adjust size:</label>
            <span id="sizebuttons">
                <button onclick="increaseImageSize()">+</button>
                &nbsp;
                <button onclick="decreaseImageSize()">-</button>
            </span>
            &nbsp; &nbsp; &nbsp;
            <label for="size">Size:</label>
            <g:textField name="size" id="size" readonly="readonly"
                         style="background-color:transparent;border: 0px solid;"></g:textField>
            <div id="canvas"></div>
        </div>
    </content>

    <content tag="description">
        Select a 3D model that will be merged to the background.  Then adjust the size of the model so that it is the desired size.  Then, click 'Continue'
        to set the location; or, click 'Advanced' to set the lighting for the model.
    </content>

    <content tag="nav">
        <g:form name="nav" action="finishSelectObject">
            <g:hiddenField id="modelId" name="modelId" value=""/>
            <g:hiddenField id="scaleData" name="scaleData" value=""/>
            <g:submitButton name="next" onclick="storeScale(scale);" class="${disabledClass}  wizard-next-button"
                            value="Continue"
                            disabled="${disabledString}"/>
            <g:submitButton name="next" class="${disabledClass}  wizard-next-button" value="Advanced"
                            disabled="${disabledString}"
                            onclick="storeScale(scale);"/>
        </g:form>
    </content>

</g:applyLayout>

