/*
 * -------------------------------------------------------------------------
 * selectLocation handles all process of the user selecting where on the background image the
 * model is placed.
 * -------------------------------------------------------------------------
 */

// enumeration of the types of location
var objectLocationEnum = {
    Centered: 0,
    Random: 1,
    Point: 2,
    Multiline: 3,
    Rectangle: 4,
    Polygon: 5
}

var locationType = objectLocationEnum.Centered;

/* DrawLayer sits on the canvas, allowing us to draw point, line, or polygon on it */
var drawLayer = new Kinetic.Layer();

/* Points is the array of Kinetic Points clicked by the user */
var points = new Array();

/* Distance at which to add a point vice remove one */
var DIST_LIMIT = 45;

/* parameters for drawing on top of image */
var OVERLAY_RADIUS = 5;
var OVERLAY_OPACITY = 0.9;
var AREA_OPACITY = 0.5;
var OVERLAY_COLOR = 'black';
var OVERLAY_FILL = 'white';
var OVERLAY_STROKE_WIDTH = 2;

/*
 * Called when the page is initialized:
 *    Add the handler to the resizeObject.js kineticCanvas (use getKineticCanvas to get it)
 *    Add the location-specific handler to it
 *    Initialize the location type
 *    Draw all the stored points (if any)
 */
function canvasInitializedCallback() {
    var kineticCanvas = getKineticCanvas();
    kineticCanvas.add(drawLayer);
    // Note that we want 'contentClick', not 'click'
    kineticCanvas.on('contentClick', pointClickHandler);

    // location type button is set in html from controller.  Use that to initialize
    setLocationTypeFromSelectedButton();
    drawInitialPoints();
}

/**
 * Draw appropriate (point, line, area) points on page startup.
 */
function drawInitialPoints() {

    switch (locationType) {
        case objectLocationEnum.Centered:
            // do not draw anything
            return;

        case objectLocationEnum.Random:
            setRandomPoints();
            return;

        case objectLocationEnum.Point:
            drawPointAsPoint();
            break;

        case objectLocationEnum.Multiline:
            drawPointsAsLine();
            break;

        case objectLocationEnum.Rectangle:
            drawPointsAsPolygon();
            break;

        case objectLocationEnum.Polygon:
            drawPointsAsPolygon();
            break;
    }
}

/**
 * Called when the user selects a location type radio button.  Set the internal location type
 * and then determine whether or not to disable the continue button.
 */
function selectLocationButtonHandler() {
    setLocationTypeFromSelectedButton();
    clearDrawLayer();
    points = new Array();

    // Centered and Random should allow continue, otherwise disable continue
    if (locationType > objectLocationEnum.Random) {
        disableContinueButton();
    }
    else {
        enableContinueButton();
    }

    // Random should show the entire area selected
    if (locationType == objectLocationEnum.Random) {
        setRandomPoints();
    }
}

/**
 * Set the LocationType variable from which location type radio button is selected
 */
function setLocationTypeFromSelectedButton() {
    var locationTypeValue = $("input[name='locationGroup']:checked").val();

    if (locationTypeValue == 'Centered') {
        locationType = objectLocationEnum.Centered;
    }
    else if (locationTypeValue == 'Random') {
        locationType = objectLocationEnum.Random;
    }
    else if (locationTypeValue == 'Point') {
        locationType = objectLocationEnum.Point;
    }
    else if (locationTypeValue == 'Multiline') {
        locationType = objectLocationEnum.Multiline;
    }
    else if (locationTypeValue == 'Rectangle') {
        locationType = objectLocationEnum.Rectangle;
    }
    else if (locationTypeValue == 'Polygon') {
        locationType = objectLocationEnum.Polygon;
    }
    else {
    }

    setDescriptionText();
}

function clearPoints() {
    selectLocationButtonHandler();
}

function disableContinueButton() {
    $("#next").attr("disabled", "disabled");
    $("#next").addClass("sightt-disabled");
}

function enableContinueButton() {
    $("#next").removeClass("sightt-disabled");
    $("#next").removeAttr("disabled");
}

/*
 * Callback when the user clicks a point on the canvas.  Get the point
 * and draw it on the canvas as appropriate
 */
function pointClickHandler() {
    var kineticPoint = getClickPoint();

    switch (locationType) {
        case objectLocationEnum.Centered:
        case objectLocationEnum.Random:
            return;

        case objectLocationEnum.Point:
            setCurrentPointAsOnlyPoint(kineticPoint);
            break;

        case objectLocationEnum.Multiline:
            addPointToLine(kineticPoint);
            break;

        case objectLocationEnum.Rectangle:
            addPointToRectangle(kineticPoint);
            break;

        case objectLocationEnum.Polygon:
            addPointToPolygon(kineticPoint);
            break;

        default:
    }
}

/*
 * Get the mouse position, and create a Kinetic Point.
 * Note that you cannot use x/y directly later, you have to use getPosition().
 */
function getClickPoint() {
    var mousePos = kineticCanvas.getPointerPosition();
    var x = mousePos.x;
    var y = mousePos.y;
    var kineticPoint = getKineticPoint(x, y);
    return kineticPoint
}

function getKineticPoint(x, y) {
    var kineticPoint = new Kinetic.Circle({
        x: x,
        y: y,
        radius: OVERLAY_RADIUS,
        fill: OVERLAY_FILL,
        stroke: OVERLAY_COLOR,
        strokeWidth: OVERLAY_STROKE_WIDTH,
        opacity: OVERLAY_OPACITY
    });
    return kineticPoint;
}

/*
 * When the user selects 'Random', then create a box over the entire image
 */
function setRandomPoints() {

    var canvas = getKineticCanvas();
    var h = canvas.getHeight();
    var w = canvas.getWidth();
    var margin = OVERLAY_RADIUS;

    var kineticPoint0 = getKineticPoint(margin, margin);
    points.push(kineticPoint0);

    var kineticPoint1 = getKineticPoint(margin, h - margin);
    points.push(kineticPoint1);

    var kineticPoint2 = getKineticPoint(w - margin, h - margin);
    points.push(kineticPoint2);

    var kineticPoint3 = getKineticPoint(w - margin, margin);
    points.push(kineticPoint3);

    drawPointsAsPolygon();
}

/*
 * If user clicks a point, add it to the line; unless it is close to an existing point,
 * remove that point.
 */
function addPointToLine(kineticPoint) {
    var index = getIndexOfClosestPoint(kineticPoint);
    if (index > -1) {
        points.splice(index, 1);
    }
    else {
        points.push(kineticPoint);
    }
    drawPointsAsLine();
}

/*
 * Go through all the points, see if they are closer than a limit, and, if
 * one of them is, return the index of it
 */
function getIndexOfClosestPoint(kineticPoint) {
    var pt1 = kineticPoint.getPosition();
    for (var ii = 0; ii < points.length; ii++) {
        var pt2 = points[ii].getPosition();
        var dist = getSquareDistance(pt1, pt2);
        if (dist < DIST_LIMIT) {
            return ii;
        }
    }
    return -1;
}

function getSquareDistance(pt1, pt2) {
    var dist = (pt1.x - pt2.x) * (pt1.x - pt2.x) + (pt1.y - pt2.y) * (pt1.y - pt2.y);
    return dist;
}

function drawPointsAsLine() {

    clearDrawLayer();

    // Determine what to do, based on number of points user has clicked: If 0 points, disable continue button
    // and return; if 1 point, disable continue and draw the point; 2 or more, enable continue and draw points / lines
    var numPoints = points.length;
    if (numPoints == 0) {
        disableContinueButton();
        return;
    }
    else if (numPoints == 1) {
        disableContinueButton();
    }
    else {
        enableContinueButton();
    }

    var initPoint = points[0];
    addPointToLayer(initPoint);

    for (var ii = 1; ii < numPoints; ii++) {

        // Add the point
        var pt = points[ii];
        addPointToLayer(pt);

        // Add a line from previous point to this one
        var initPtPos = initPoint.getPosition();
        var ptPos = pt.getPosition();
        var line = new Kinetic.Line({
            points: [initPtPos.x, initPtPos.y, ptPos.x, ptPos.y],
            stroke: OVERLAY_COLOR,
            strokeWidth: OVERLAY_STROKE_WIDTH,
            opacity: OVERLAY_OPACITY
        });
        drawLayer.add(line);
        initPoint = pt;
    }
    drawLayer.draw();
}

// Draw a point on the layer.  However, draw another circle of
// contrasting color first to make it more visible.
function addPointToLayer(pt) {

    var ptPos = pt.getPosition();

    var newPoint = new Kinetic.Circle({
        x: ptPos.x,
        y: ptPos.y,
        radius: OVERLAY_RADIUS + 1,
        fill: OVERLAY_COLOR,
        stroke: OVERLAY_FILL,
        strokeWidth: 0,
        opacity: OVERLAY_OPACITY
    });

    drawLayer.add(newPoint);
    drawLayer.add(pt);
}

function addPointToRectangle(kineticPoint) {
    var index = getIndexOfClosestPoint(kineticPoint);
    if (index > -1) {
        points.splice(index, 1);
    }
    else {
        while (points.length > 1) {
            points.pop();
        }
        points.push(kineticPoint);
    }
    drawPointsAsRectangle();

    if (points.length == 2) {
        enableContinueButton();
    }
    else {
        disableContinueButton();
    }
}

function drawPointsAsRectangle() {

    clearDrawLayer();

    var numPoints = points.length;
    if (numPoints == 0) {
        disableContinueButton();
        return;
    }

    // Draw the points
    var minX = 1000000;
    var minY = 1000000;
    var maxX = -1;
    var maxY = -1;
    for (var ii = 0; ii < numPoints; ii++) {
        var pt = points[ii];

        addPointToLayer(pt);
        var loc = pt.getPosition();

        if (loc.x > maxX) maxX = loc.x;
        if (loc.x < minX) minX = loc.x;
        if (loc.y > maxY) maxY = loc.y;
        if (loc.y < minY) minY = loc.y;
    }

    if (numPoints == 2) {

        var pointsAsList = [];
        pointsAsList.push(minX, minY);
        pointsAsList.push(minX, maxY);
        pointsAsList.push(maxX, maxY);
        pointsAsList.push(maxX, minY);

        var polygon = new Kinetic.Line({
            points: pointsAsList,
            fill: OVERLAY_FILL,
            stroke: OVERLAY_COLOR,
            strokeWidth: OVERLAY_STROKE_WIDTH,
            opacity: AREA_OPACITY,
            closed: true
        });

        drawLayer.add(polygon);
    }
    drawLayer.draw();
}

function addPointToPolygon(kineticPoint) {
    var index = getIndexOfClosestPoint(kineticPoint);
    if (index > -1) {
        points.splice(index, 1);
    }
    else {
        points.push(kineticPoint);
    }
    drawPointsAsPolygon();
}

function drawPointsAsPolygon() {

    clearDrawLayer();

    var numPoints = points.length;
    if (numPoints == 0) {
        disableContinueButton();
        return;
    }
    else if (numPoints == 1 || numPoints == 2) {
        disableContinueButton();
    }
    else {
        enableContinueButton();
    }

    var pointsAsList = [];
    for (var ii = 0; ii < points.length; ii++) {
        var pt = points[ii];
        addPointToLayer(pt);
        var loc = pt.getPosition();
        pointsAsList.push(loc.x, loc.y);
    }

    var polygon = new Kinetic.Line({
        points: pointsAsList,
        fill: OVERLAY_FILL,
        stroke: OVERLAY_COLOR,
        strokeWidth: OVERLAY_STROKE_WIDTH,
        opacity: AREA_OPACITY,
        closed: true
    });

    drawLayer.add(polygon);
    drawLayer.draw();
}

function setCurrentPointAsOnlyPoint(kineticPoint) {
    points = new Array();
    points.push(kineticPoint);
    drawPointAsPoint();
}

function drawPointAsPoint() {
    clearDrawLayer();
    if (points.length < 1) {
        disableContinueButton();
        return;
    }
    enableContinueButton();
    addPointToLayer(points[0]);
    drawLayer.draw();
}

function clearDrawLayer() {
    drawLayer.removeChildren();
    drawLayer.draw();
}

/**
 * During initialization, the points saved in the session will get passed in.
 * The points are in a string of Position Objects, containing and x and a y
 */
function setPointsFromJSON(pointListString) {
    points = [];
    if (pointListString == null || pointListString.length == 0) {
        return;
    }
    // Convert them from the JSON string to an array of Position (x,y) objects
    var positionList = JSON.parse(pointListString);

    // For each element of the string, add a Kinetic Point
    for (var ii = 0; ii < positionList.length; ii++) {
        var pos = positionList[ii];
        points.push(getKineticPoint(pos.x, pos.y));
    }
}

/**
 * Get a string, in JSON format, of the (x,y) locations of the points, by
 * first converting to a list of Position (x,y) objects and then
 * calling stringify on the list.
 */
function getPointsJSON() {
    var positionList = [];
    for (var ii = 0; ii < points.length; ii++) {
        var pt1 = points[ii].getPosition();
        positionList.push(pt1)
    }
    var pointString = JSON.stringify(positionList);
    return pointString;
}


function setDescriptionText() {
    var desc = "";

    switch (locationType) {
        case objectLocationEnum.Centered:
            desc = "The model center will be centered in the image";
            break;

        case objectLocationEnum.Random:
            desc = "The model center will be randomly placed in the image"
            break;

        case objectLocationEnum.Point:
            desc = "Click a point in the image, and the model center will be placed at that point"
            break;

        case objectLocationEnum.Multiline:
            desc = "Click a point in the image to set the beginning of the line; click more points " +
                "to form a multiline.   The model center will be randomly placed on the multiline.  To remove" +
                " a point, click on it";
            break;

        case objectLocationEnum.Rectangle:
            desc = "Click a point in the image to set the top left point.  Click another point to create a " +
                "rectangle.  The model center will be randomly placed in the rectangle.  To remove" +
                " a point, click it";
            break;

        case objectLocationEnum.Polygon:
            desc = "Click a point in the image to set an initial point.  Click additional points to " +
                "set points in the polygon.  The model center will be randomly placed in the polygon.  To remove" +
                " a point, click on it.";
            break;
    }

    $("#locationDesc").val(desc)

}
