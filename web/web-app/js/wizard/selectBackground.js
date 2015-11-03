var backgroundDropzone;
var displayedBackground;

function createBackgroundDropzone(saveLink) {
    backgroundDropzone = new Dropzone(document.body, {
        url: saveLink,
        previewsContainer: "#uploadPreview",
        clickable: "#uploadButton",
        paramName: "backgroundFile",
        params: { origin: 'wizard' }
    });

    backgroundDropzone.on("addedfile", function (file) {
        changeUploadText("Uploading " + file.name + "...", "message");
    });

    backgroundDropzone.on("complete", function (file) {
        location.reload();
    });
}

function displayBackground(background, backgroundAmazonLink) {
    displayedBackground = background;

    //calculate image parameters.
    var imageWidth = background.width;
    var imageHeight = background.height;
    var scaledWidth = 540;
    var scaleFactor = scaledWidth / imageWidth;
    var scaledHeight = imageHeight * scaleFactor;

    var scaledWidthStr = "" + scaledWidth + "px";
    var scaledHeightStr = "" + scaledHeight + "px";

    $("#backImage").html(
        $('<div>', {id: "image", width: scaledWidthStr, height: scaledHeightStr})
            .css({ "background-image": "url(" + backgroundAmazonLink + ")", "background-size": scaledWidthStr, "border": "1px solid #3377DD"})
    );

    $("#previewText").hide();
    $("#preview-filename").text(background.name);

    addBackground();
}


function selectBackground(background) {
    var url = backgroundUrls[background.id] || null;
    if (url == null) {
        $.post(backgroundLinkAddr, background, function (backgroundAmazonLink) {
            backgroundUrls[background.id] = backgroundAmazonLink;
            displayBackground(background, backgroundAmazonLink);
        });
    } else {
        displayBackground(background, url);
    }
}

function addBackground() {

    clearBackgroundsList();

    selectedBackgrounds.push(displayedBackground.id);
    selectedBackgroundReferences[displayedBackground.id] = {
        id: displayedBackground.id,
        name: displayedBackground.name,
        width: displayedBackground.width,
        height: displayedBackground.height,
        filePath: displayedBackground.filePath
    };
    generateSelectedBackgroundsListDisplay();
    $("[name='next']").removeClass("sightt-disabled");
    $("[name='next']").removeAttr("disabled");
}

function clearBackgroundsList() {
    while (selectedBackgrounds.length > 0) {
	selectedBackgrounds.pop();
    }

    for (ref in selectedBackgroundReferences) {
	delete selectedBackgroundReferences[ref];
    }
}

function removeBackground(event) {
    var selectedIndex = selectedBackgrounds.indexOf(displayedBackground.id);
    if (selectedIndex >= 0) {
        selectedBackgrounds.splice(selectedIndex, 1);
        delete selectedBackgroundReferences[displayedBackground.id];
    }
    updateImageButtons();
    generateSelectedBackgroundsListDisplay();
    if (selectedBackgrounds.length < 1) {
        $("[name='next']").addClass("sightt-disabled");
        $("[name='next']").attr("disabled", "disabled");
    }
}

function backgroundSelected(background) {
    var selected = selectedBackgrounds.indexOf(background.id) >= 0;
    return selected;
}

function selectBackgroundFromCarousel(background) {
    selectBackground(background);
}

function changeUploadText(htmlText, divClass) {
    $("#uploadText").show();
    $("#uploadText").html(htmlText);
    $("#uploadText").removeClass("confirm message");
    $("#uploadText").addClass(divClass);
}

function setBackgroundIdsFromList() {
    $("#backgroundIds").val(JSON.stringify(selectedBackgrounds));
}

function generateSelectedBackgroundsListDisplay() {
    var listDiv = $("#selectedBackgroundsList");
    listDiv.empty();
    for (var key in selectedBackgroundReferences) {
        var tmp = selectedBackgroundReferences[key];
	var displayString = tmp.name + "  (" + tmp.width + "x" + tmp.height + ")";

        listDiv.append($('<p>').text(displayString).click((function (background) {
            return function () {
                selectBackground(background);
            }
        })(tmp)));
    }
}
