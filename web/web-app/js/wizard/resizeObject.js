var kineticCanvas;
var scale;
var scaleFactor = .05;
var scaleAmount;

var backgroundLayer = new Kinetic.Layer();
var foregroundLayer = new Kinetic.Layer();

var foregroundKineticImage;

// Clark commented because it appears that it's not used anywhere
// var backgroundKineticImage;

var canvasSize = {
    width: 540,
    height: 0
};

var canvasLoaded = false;

function getKineticCanvas() {
    return kineticCanvas;
}

function loadResizeCanvas(scaleData, backgroundUrl, renderedModelUrl, callback) {
    if (!scaleData) {
        scale = 1.0;
    }
    else {
        scale = scaleData;
    }
    refreshCanvas(backgroundUrl, renderedModelUrl, callback);
}

function resetScale() {
    scale = 1.0;
}

$.loadImageDeferred = function (url) {
    var loadImage = function (deferred) {
        var image = new Image();

        image.onload = loaded;
        image.onerror = errorOccured;
        image.onabort = errorOccured;

        image.src = url;

        function loaded() {
            unbindEvents();
            deferred.resolve(image);
        }

        function errorOccured() {
            unbindEvents();
            deferred.reject(image);
        }

        function unbindEvents() {
            image.onload = null;
            image.onerror = null;
            image.onabort = null;
        }
    };
    return $.Deferred(loadImage).promise();
}

function refreshCanvas(backgroundUrl, renderedModelUrl, callback) {
    var loadBackground = $.loadImageDeferred(backgroundUrl);
    var loadModel = null;  //Causes the $.when() call to ignore this promise
    if (renderedModelUrl != null) {
        loadModel = $.loadImageDeferred(renderedModelUrl);
    }

    $.when(loadBackground, loadModel).done(function (bgImage, modelImage) {
        backgroundImage = bgImage
        resetCanvas();
        var kineticBackground = createKineticBackground();

        initializeCanvas();
        backgroundLayer.add(kineticBackground);
        kineticCanvas.draw();

        if (modelImage) {
            foregroundKineticImage = new Kinetic.Image({
                id: "scalingImage",
                image: modelImage,
                width: modelImage.width,
                height: modelImage.height
            });
            scaleAmount = foregroundKineticImage.getHeight() * scaleFactor;

            scaleImage();
            foregroundLayer.add(foregroundKineticImage);
            kineticCanvas.draw();
        }

        canvasLoaded = true;
        if (callback) {
            callback();
        }
    });
}

function resetCanvas() {
    canvasSize.width = $('#canvasWrapper').parent().width();
    backgroundLayer.removeChildren();
    foregroundLayer.removeChildren();
}

function initializeCanvas() {
    kineticCanvas = new Kinetic.Stage({
        container: 'canvas',
        width: canvasSize.width,
        height: canvasSize.height
    });
    kineticCanvas.add(backgroundLayer);
    kineticCanvas.add(foregroundLayer);
}

function increaseImageSize() {
    var newHeight = foregroundKineticImage.getHeight() + scaleAmount;
    scale *= (newHeight / foregroundKineticImage.getHeight());
    changeSize(newHeight);
}

function decreaseImageSize() {
    var newHeight = foregroundKineticImage.getHeight() - scaleAmount;
    scale *= (newHeight / foregroundKineticImage.getHeight());
    changeSize(newHeight);
}

function scaleImage() {
    changeSize(foregroundKineticImage.getHeight() * scale);
}

function changeSize(newHeight) {
    var aspectRatio = foregroundKineticImage.getWidth() / foregroundKineticImage.getHeight();
    var width = aspectRatio * newHeight;

    foregroundKineticImage.setHeight(newHeight);
    foregroundKineticImage.setWidth(width);

    // Set the size variable to show user
    scaleText = "" + (scale * 100).toFixed(1) + "%"
    $("#size").val(scaleText)

    centerImage();
}

function loadImage(imageUrl, image, onloadFunction) {
    image.onload = onloadFunction;
    image.src = imageUrl;
}

function createKineticBackground() {
    scaleBackgroundToCanvas();

    return new Kinetic.Image({
        id: "backgroundImage",
        image: backgroundImage
    });
}

function scaleBackgroundToCanvas() {
    var width = backgroundImage.width;
    var height = backgroundImage.height;
    var aspectRatio = height / width;

    var newWidth = canvasSize.width;
    var newHeight = newWidth * aspectRatio;

    // Commented code because we don't want to change for height.  Tall images will go way vertically
    //    if (newHeight > canvasSize.height) {
    //        newHeight = canvasSize.height;
    //        newWidth = newHeight / aspectRatio;
    //    }

    backgroundImage.width = newWidth;
    backgroundImage.height = newHeight;
    canvasSize.height = newHeight;
}

function centerImage() {
    var centerX = canvasSize.width / 2;
    var centerY = canvasSize.height / 2;

    var x = centerX - (foregroundKineticImage.getWidth() / 2);
    var y = centerY - (foregroundKineticImage.getHeight() / 2);

    foregroundKineticImage.setX(x);
    foregroundKineticImage.setY(y);

    foregroundLayer.draw();
}
