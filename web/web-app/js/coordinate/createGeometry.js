//Creates a coordinate system on a drawing.
var canvas;

var scalingImageLayer = new Kinetic.Layer();
var coordsLayer = new Kinetic.Layer();
var messageLayer = new Kinetic.Layer();

var backgroundImage = new Image();

//Constants and storage of the drawn points and lines.
var coordinateSystem = {
    canvasWidth: 900,
    canvasHeight: 700,
    imageOffsetX: 50,
    imageOffsetY: 50,
    points: new Array(4),
    lines: new Array(3)
};

var drawingPoints = {
    //Keeps track of which point have last placed on the canvas, while we are drawing the points/lines.
    currentPointIndex: 0,
    //Keeps track of the last point clicked, for viewing/setting length in meters.
    selectedPoint: null
};

/**
 * This function kicks off drawing a geometry on a background image.
 * @param canvasJson The existing json. Will be '' if this image's geometry has not been saved.
 *                   Otherwise, it's the JSON of the saved canvas. */
function createGeometry(canvasJson) {
    if (canvasJson) {
        loadFromServer(canvasJson);
        loadScalingImageFromServer();
        return;
    }
    createNewCanvas();
}

function loadFromServer(canvasJson) {
    canvas = Kinetic.Node.create(canvasJson, 'canvas');
    initFromLoadedCanvas();
    loadImage(backgroundUrl, backgroundImage, function () {
        canvas.get('#background')[0].setImage(backgroundImage);
        for (var i = 0; i < 4; i++) {
            addPointHandlers(coordinateSystem.points[i], i);
        }
        canvas.draw();
        writeMessage("You may drag the points to update the room outline. Click a point to view its length.");
    });
}

function initFromLoadedCanvas() {
    coordinateSystem.points[0] = canvas.get('#origin')[0];
    coordinateSystem.points[1] = canvas.get('#xpoint')[0];
    coordinateSystem.points[2] = canvas.get('#ypoint')[0];
    coordinateSystem.points[3] = canvas.get('#zpoint')[0];
    coordinateSystem.lines[0] = canvas.get('#zline')[0];
    coordinateSystem.lines[1] = canvas.get('#yline')[0];
    coordinateSystem.lines[2] = canvas.get('#xline')[0];

    //children[0] is the backgroundLayer
    scalingImageLayer = canvas.children[1];
    coordsLayer = canvas.children[2];
    messageLayer = canvas.children[3];
}

function loadImage(imageUrl, image, onloadFunction) {
    image.onload = onloadFunction;
    image.src = imageUrl;
}

function createNewCanvas() {
    initializeCanvas();
    initializeLines();
    loadImage(backgroundUrl, backgroundImage, function () {
        var kineticBackground = createKineticBackgroundImage();
        var backgroundLayer = new Kinetic.Layer();
        backgroundLayer.add(kineticBackground);
        canvas.add(backgroundLayer);
        canvas.add(scalingImageLayer);
        canvas.add(coordsLayer);
        canvas.add(messageLayer);
        coordsLayer.setListening(false);
        writeMessage("Please place a line in a corner of the room by clicking the corner.");
    });
    addCanvasHandlers();
}

function initializeCanvas() {
    canvas = new Kinetic.Stage({
        container: 'canvas',
        width: coordinateSystem.canvasWidth,
        height: coordinateSystem.canvasHeight
    });
}

function createKineticBackgroundImage() {
    scaleImageToCanvas();

    return new Kinetic.Image({
        id: "background",
        x: coordinateSystem.imageOffsetX,
        y: coordinateSystem.imageOffsetY,
        image: backgroundImage
    });
}

function scaleImageToCanvas() {
    var width = backgroundImage.width;
    var height = backgroundImage.height;
    var aspectRatio = width / height;

    var newHeight = coordinateSystem.canvasHeight;
    var newWidth = newHeight * aspectRatio;
    if (newWidth > coordinateSystem.canvasWidth) {
        newWidth = coordinateSystem.canvasWidth;
        newHeight = newWidth / aspectRatio;
    }
    backgroundImage.width = newWidth;
    backgroundImage.height = newHeight;
}

/**
 * It's convenient to create the lines and place them on the canvas up front. We later
 * change the line's points array on mouse move to physically draw the line.
 */
function initializeLines() {
    createLine(0, 'z', 'red');
    createLine(1, 'y', 'blue');
    createLine(2, 'x', 'green');
    coordinateSystem.moveHandler = moveVerticalLine;
}

function createLine(index, direction, color) {
    coordinateSystem.lines[index] = new Kinetic.Multiline({
        id: direction + "line",
        points: [0, 0, 0, 0],
        stroke: color,
        strokeWidth: 3
    });
    coordsLayer.add(coordinateSystem.lines[index]);
}

function addCanvasHandlers() {
    canvas.on('mousemove', function () {
        coordinateSystem.moveHandler();
    });
    canvas.on('click', placePoint);
}

/**
 * 'click' handler for when the user clicks to place each line. This draws a point where the user clicked,
 * and updates the move handler to draw the next line.
 */
function placePoint() {
    if (drawingPoints.currentPointIndex === 0) {
        addPoint('origin');
        writeMessage("Please place a line outlining the floor plane.");
    }
    if (drawingPoints.currentPointIndex === 1) {
        addPoint('xpoint');
        writeMessage("Please place another line outlining the floor plane.");
    }
    if (drawingPoints.currentPointIndex === 2) {
        addPoint('ypoint');
        writeMessage("You may drag the points to update the room outline. Click save to save your work.");
    }
    drawingPoints.currentPointIndex++;

    if (drawingPoints.currentPointIndex === 3) {
        addPoint('zpoint');
        placingPointsComplete();
        return;
    }
    coordinateSystem.moveHandler = movePerspectiveLine;
}

function addPoint(uid) {
    var mousePosition = canvas.getMousePosition();
    var x = mousePosition.x;
    var y = mousePosition.y;
    if (uid === 'zpoint') {
        x = coordinateSystem.points[0].getX();
        y = coordinateSystem.imageOffsetY;
    }

    var point = new Kinetic.Circle({
        id: uid,
        x: x,
        y: y,
        radius: 6,
        fill: 'white',
        stroke: 'purple',
        strokeWidth: 2
    });
    coordsLayer.add(point);
    coordinateSystem.points[drawingPoints.currentPointIndex] = point;
    coordsLayer.draw();
}

/**
 * Initial 'mousemove' handler that draws a vertical line
 */

function moveVerticalLine() {
    var mousePosition = canvas.getMousePosition();
    coordinateSystem.lines[0].setPoints([mousePosition.x, mousePosition.y, mousePosition.x, coordinateSystem.imageOffsetY]);
    coordsLayer.draw();
}

/**
 * 'mousemove' handler that draws a line from the mouse to the origin.
 */
function movePerspectiveLine() {
    var mousePosition = canvas.getMousePosition();
    var origin = coordinateSystem.lines[0].getPoints()[0];
    coordinateSystem.lines[drawingPoints.currentPointIndex].setPoints([origin.x, origin.y, mousePosition.x, mousePosition.y]);
    coordsLayer.draw();
}

/**
 * Removes the mouse listeners on the canvas and sets listeners on the points. Loads the scaling image.
 */
function placingPointsComplete() {
    canvas.off('mousemove click');
    for (var i = 0; i < 4; i++) {
        coordinateSystem.points[i].setDraggable(true);
        addPointHandlers(coordinateSystem.points[i], i);
    }
    coordsLayer.setListening(true);
    coordsLayer.draw();
    loadScalingImage(); //loadHumanImage is in insertScalingImage.js
}

/**
 * Adds dragging and click functionality to points.
 * @param point
 * @param index
 */

function addPointHandlers(point, index) {
    point.on('dragmove', function () {
        redrawLine(index);
    });
    point.on('mouseover', function () {
        document.body.style.cursor = 'pointer';
        point.setFill('yellow');
    });
    point.on('mouseout', function () {
        document.body.style.cursor = 'default';
        point.setFill('white');
        coordsLayer.draw();
    });
    point.on('click', function (clickEvent) {
        if (point.getId() === "origin")
            return;
        drawingPoints.selectedPoint = point;
        showSetLengthForm(clickEvent);
    });
}

/**
 * 'dragmove' handler for a point. Redraws the lines attached to the points.
 * This differs depending on which point dragged.
 * @param index Indicates which point is being dragged. 0 is the origin, 1-3 are the y,x,z points. See placePoint().
 */

function redrawLine(index) {
    var origin = coordinateSystem.points[0];

    if (index === 0) {
        coordinateSystem.lines[0].setPoints([origin.getX(), origin.getY(), origin.getX(), coordinateSystem.imageOffsetY]);
        setPointsForPerspectiveLine(origin, 1);
        setPointsForPerspectiveLine(origin, 2);
        setPointsForPerspectiveLine(origin, 3);
    }
    else {
        setPointsForPerspectiveLine(origin, index);
    }
    coordinateSystem.points[0].moveToTop();
    canvas.draw();
    drawScalingLines();
    setDistances();
}

/**
 * Draws the line from the origin to the current point.
 * @param origin
 * @param index of the current point
 */
function setPointsForPerspectiveLine(origin, pointIndex) {
    var lineIndex = pointIndex;
    if (pointIndex === 3) {
        lineIndex = 0;
    }

    if (coordinateSystem.lines[lineIndex]) {
        var point = coordinateSystem.points[pointIndex];
        coordinateSystem.lines[lineIndex].setPoints([origin.getX(), origin.getY(), point.getX(), point.getY()]);
        point.moveToTop();
    }
}

/**
 * Write a message on the top of the canvas.
 * @param message
 */
function writeMessage(message) {
    var context = messageLayer.getContext();
    messageLayer.clear();
    context.font = '12pt Calibri';
    context.fillStyle = 'black';
    context.fillText(message, 5, 25);
}

/**
 * Show the hidden div that displays the meter value and a way to change it.
 * @param clickEvent
 */
function showSetLengthForm(clickEvent) {
    var cssObj = {
        position: "absolute",
        top: (clickEvent.y),
        left: (clickEvent.x + 20),
        display: "block"
    };
    var attrs = getLineFromSelectedPoint().getAttrs();
    $("#lengthInMeters").val(attrs.meters);
    var div = $("#lengthPicturedDiv");
    div.css('z-index', 10);
    div.css(cssObj);
}

/**
 * Called by the apply button on the meter value form.
 */

function applyLength() {
    var attrs = getLineFromSelectedPoint().getAttrs();
    attrs.meters = $("#lengthInMeters").val();
    $("#lengthPicturedDiv").css('display', 'none');
}

/**
 * Called by the cancel button on the meter value form.
 */

function cancelLength() {
    $("#lengthPicturedDiv").css('display', 'none');
}

function getLineFromSelectedPoint() {
    if (drawingPoints.selectedPoint.getId() === "xpoint") {
        return coordinateSystem.lines[1];
    }
    if (drawingPoints.selectedPoint.getId() === "ypoint") {
        return coordinateSystem.lines[2];
    }
    if (drawingPoints.selectedPoint.getId() === "zpoint") {
        return coordinateSystem.lines[0];
    }
}

/**
 * Save geometry by posting to the controller the canvas JSON.
 * Invoked by the Save Geometry button.
 */
function save() {
    $.ajax({
        type: "POST",
        url: postUrl,
        contentType: "text/plain",
        dataType: "json",
        data: canvas.toJSON(),
        success: function () {
            writeMessage("Geometry saved successfully.");
        },
        error: function () {
        }
    });
}



