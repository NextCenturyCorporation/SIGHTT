var scalingImage = new Image();
var scalingGroup;
var kineticImage;

var scalingLines = new Array(2);

/**
 * Create lines that define the plane on which the scaling image is standing.
 * It's useful to create them up front, then move them along with the image
 */
function initializeScalingLines() {
    scalingLines[0] = new Kinetic.Multiline({
        id: "scaleOneLine",
        points: [0, 0, 0, 0],
        stroke: 'purple',
        dashArray: [25, 10],
        strokeWidth: 3
    });
    scalingLines[1] = new Kinetic.Multiline({
        id: "scaleTwoLine",
        points: [0, 0, 0, 0],
        stroke: 'purple',
        dashArray: [25, 10],
        strokeWidth: 3
    });
    scalingImageLayer.add(scalingLines[0]);
    scalingImageLayer.add(scalingLines[1]);
}

//canvas and loadImage() are from createGeometry.js
function loadScalingImageFromServer() {
    loadImage(scalingUrl, scalingImage, function () {
        scalingLines[0] = canvas.get('#scaleOneLine')[0];
        scalingLines[1] = canvas.get('#scaleTwoLine')[0];
        kineticImage = canvas.get('#scaledImage')[0];
        kineticImage.setImage(scalingImage);
        scalingGroup = canvas.get('#scaledGroup')[0];
        scalingGroup.setDragBoundFunc(boundScalingImageToFloor);
        canvas.draw();
        addSizingHandlers();
        addPlaneLineHandlers();
    });
}

//Invoked by createGeometry.js                                                                                                                                                                                                                                                                                +++++teGeometry.js
function loadScalingImage() {
    initializeScalingLines();
    loadImage(scalingUrl, scalingImage, function () {
        kineticImage = new Kinetic.Image({
            id: "scaledImage",
            image: scalingImage,
            width: scalingImage.width,
            height: scalingImage.height,
            opacity: 0.7
        });

        scalingGroup = createScalingGroup();
        var increase = createStyledSizerArrows(true);
        var decrease = createStyledSizerArrows(false);

        scalingGroup.add(kineticImage);
        scalingGroup.add(increase);
        scalingGroup.add(decrease);

        scalingImageLayer.add(scalingGroup);
        scalingImageLayer.draw();

        addSizingHandlers();
        addPlaneLineHandlers();
        setDistances();
    });
}

/**
 * Draggable group of a known sized image. The group contains the image and resize buttons.
 * @return {Kinetic.Group}
 */
function createScalingGroup() {
    var initialPos = boundScalingImageToFloor(untranslateImagePoint({
        x: coordinateSystem.points[0].getX(),
        y: coordinateSystem.points[0].getY()
    }, kineticImage));

    return new Kinetic.Group({
        id: "scaledGroup",
        x: initialPos.x,
        y: initialPos.y,
        draggable: true,
        dragBoundFunc: boundScalingImageToFloor
    });
}

/**
 * Creates the up and down arrows
 * @param grow true if the up arrow, false if the down arrow
 * @return {Kinetic.RegularPolygon}
 */
function createStyledSizerArrows(grow) {
    var uid = "increaseSizer";
    var yValue = 40;
    var rotationDegrees = 0;
    if (!grow) {
        uid = "decreaseSizer";
        yValue = 65;
        rotationDegrees = 180;
    }

    return new Kinetic.RegularPolygon({
        id: uid,
        x: 0,
        y: yValue,
        sides: 3,
        radius: 10,
        fill: 'orange',
        stroke: 'black',
        strokeWidth: 2,
        rotationDeg: rotationDegrees
    });
}

/**
 * Bounds the scaling image below the 'x' (green) and 'y' (blue) lines and within the canvas.
 * @param pos
 * @return {{x: *, y: *}}
 */

function boundScalingImageToFloor(pos) {
    var pointReturnValue = {
        x: pos.x,
        y: pos.y
    };

    var bottomCenterPoint = translateImagePoint(pointReturnValue, kineticImage);

    var line1 = new LineEquation(coordinateSystem.lines[1].getPoints());
    var line2 = new LineEquation(coordinateSystem.lines[2].getPoints());

    if (line1.getMin("x") == line2.getMin("x") || line1.getMax("x") == line2.getMax("x")) {
        var line = findLineNearestToSlopeZero(line1, line2);
        line.extendPointsToMinimum();
        line.extendPointsToMaximum();
        pointReturnValue = boundBelowLine(pointReturnValue, bottomCenterPoint, line);
    }
    else {
        if (line1.getMin("x") < line2.getMin("x")) {
            line1.extendPointsToMinimum();
            line2.extendPointsToMaximum();
        }
        else {
            line2.extendPointsToMinimum();
            line1.extendPointsToMaximum();
        }
        pointReturnValue = boundBelowLine(pointReturnValue, bottomCenterPoint, line1);
        pointReturnValue = boundBelowLine(pointReturnValue, bottomCenterPoint, line2);
    }

    pointReturnValue = boundToImageEdges(pointReturnValue, bottomCenterPoint);
    return pointReturnValue;
}

/**
 * Pick the line closest to horizontal.
 */

function findLineNearestToSlopeZero(line1, line2) {
    if (Math.abs(line1.slope) < Math.abs(line2.slope)) {
        return line1;
    }
    return line2;
}

function boundBelowLine(pointReturnValue, currentPoint, line) {
    var pointOnLine = line.getPointOnLineFromXValue(currentPoint.x);
    if (pointOnLine.y > currentPoint.y && line.contains(pointOnLine.y, "y")) {
        pointReturnValue.y = untranslateImagePoint(pointOnLine, kineticImage).y;
    }
    return pointReturnValue;
}

function boundToImageEdges(pointReturnValue, bottomCenterPoint) {
    if (pointReturnValue.x < coordinateSystem.imageOffsetX) {
        pointReturnValue.x = coordinateSystem.imageOffsetX;
    }
    if (pointReturnValue.x > coordinateSystem.canvasWidth - kineticImage.getWidth()) {
        pointReturnValue.x = coordinateSystem.canvasWidth - kineticImage.getWidth();
    }
    if (bottomCenterPoint.y > coordinateSystem.canvasHeight) {
        pointReturnValue.y = coordinateSystem.canvasHeight - kineticImage.getHeight();
    }
    return pointReturnValue;
}

/**
 * Draw the two dashed lines to indicate the plane the scaling image is on.
 */

function drawScalingLines() {
    var point = {
        x: scalingGroup.getX(),
        y: scalingGroup.getY()
    };

    var bottomCenterPoint = translateImagePoint(point, kineticImage);
    var line1 = new LineEquation(coordinateSystem.lines[1].getPoints());
    var line2 = new LineEquation(coordinateSystem.lines[2].getPoints());

    var pointOnLine = findPointOnLine(line1, line2, bottomCenterPoint);
    var pointOnLine2 = findPointOnLine(line2, line1, bottomCenterPoint);

    scalingLines[0].setPoints([bottomCenterPoint.x, bottomCenterPoint.y, pointOnLine.x, pointOnLine.y]);
    scalingLines[1].setPoints([bottomCenterPoint.x, bottomCenterPoint.y, pointOnLine2.x, pointOnLine2.y]);

    scalingImageLayer.draw();
}

/**
 * The point on the intersection line that passes through the starting point with the same slope as the parallel line
 */
function findPointOnLine(parallelLine, intersectionLine, startingPoint) {
    var offsetOfNewLine = startingPoint.y - parallelLine.slope * startingPoint.x;
    var x = (intersectionLine.offset - offsetOfNewLine) / (parallelLine.slope - intersectionLine.slope);
    var point = intersectionLine.getPointOnLineFromXValue(x);

    if (point.x < coordinateSystem.imageOffsetX)
        point.x = coordinateSystem.imageOffsetX;
    if (point.y < coordinateSystem.imageOffsetY)
        point.y = coordinateSystem.imageOffsetY;

    return point;
}

/**
 * Convert the top left corner to the center middle point of an image.
 */
function translateImagePoint(point, image) {
    return {
        x: point.x + image.getWidth() / 2,
        y: point.y + (image.getHeight() - 10)
    };
}

/**
 * Convert the center middle point to the top left corner of an image.
 */
function untranslateImagePoint(point, image) {
    return {
        x: point.x - image.getWidth() / 2,
        y: point.y - (image.getHeight() - 10)
    };
}

function addPlaneLineHandlers() {
    scalingGroup.on('dragmove', drawScalingLines);
}

function addSizingHandlers() {
    var increase = canvas.get('#increaseSizer')[0];
    increase.on('click', increaseSize);

    var decrease = canvas.get('#decreaseSizer')[0];
    decrease.on('click', decreaseSize);
}

function increaseSize() {
    changeSize(kineticImage.getHeight() * 1.05);
}

function decreaseSize() {
    changeSize(kineticImage.getHeight() * 0.95);
}

/**
 * Change the size of the scaling image.
 * We make sure the image is still in the floor bounds.
 */
function changeSize(newHeight) {
    var aspectRatio = kineticImage.getWidth() / kineticImage.getHeight();
    var width = aspectRatio * newHeight;

    kineticImage.setHeight(newHeight);
    kineticImage.setWidth(width);

    var point = boundScalingImageToFloor({x: scalingGroup.getX(), y: scalingGroup.getY()});
    scalingGroup.setX(point.x);
    scalingGroup.setY(point.y);

    drawScalingLines();
    setDistances();
}

/**
 * Update the meters values of each line based on the images size.
 * Assumes the image is 1.75 meters tall.
 */
function setDistances() {
    var ratio = 1.75 / kineticImage.getHeight();
    coordinateSystem.lines.forEach(function (line) {
        var distance = distanceInPixels(line);
        line.getAttrs().meters = distance * ratio;
    });
}

/**
 * Distance formula for a line in pixels.
 * @return {Number}
 */

function distanceInPixels(line) {
    var pointsArr = line.getPoints();
    var deltaYSquared = Math.pow(pointsArr[0].y - pointsArr[1].y, 2);
    var deltaXSquared = Math.pow(pointsArr[0].x - pointsArr[1].x, 2);
    return Math.sqrt(deltaYSquared + deltaXSquared);
}