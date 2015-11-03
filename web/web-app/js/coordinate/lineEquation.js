/**
 * Object constructor that defines a line as a slope and offset.
 * Methods exist for common operations.
 * @param pointsArr two points that define the line.
 * @constructor Use like this: var line = new LineEquation(pts);
 */

function LineEquation(pointsArr) {
    //Private functions
    function yDelta(pointsArr) {
        return pointsArr[0].y - pointsArr[1].y;
    }

    function xDelta(pointsArr) {
        return pointsArr[0].x - pointsArr[1].x;
    }

    function createInternalPoints() {
        var pts = new Array(2);
        pts[0] = {x: pointsArr[0].x, y: pointsArr[0].y};
        pts[1] = {x: pointsArr[1].x, y: pointsArr[1].y};
        return pts;
    }

    var yD = yDelta(pointsArr);
    var xD = xDelta(pointsArr);
    var points = createInternalPoints();
    var slopeInfinite = true;

    if (xD !== 0) {
        slopeInfinite = false;
        //Slope an offset are publicly accessable by an object instance.
        this.slope = yD / xD;
        this.offset = pointsArr[0].y - (this.slope * pointsArr[0].x);
    }

    //Public methods
    this.getPointOnLineFromXValue = function (xValue) {
        if (slopeInfinite) {
            return {
                x: 0, y: 0
            };
        }
        return {
            x: xValue,
            y: (this.slope * xValue + this.offset)
        };
    };

    this.getMin = function (direction) {
        var min = points[0][direction];
        if (min > points[1][direction]) {
            min = points[1][direction];
        }
        return min;
    };

    this.getMax = function (direction) {
        var max = points[0][direction];
        if (max < points[1][direction]) {
            max = points[1][direction];
        }
        return max;
    };

    this.contains = function (value, direction) {
        var min = this.getMin(direction);
        var max = this.getMax(direction);
        if (value <= min || value >= max) {
            return false;
        }
        return true;
    };

    this.extendPointsToMinimum = function () {
        var indexOfMin = (this.getMin("x") == points[0].x) ? 0 : 1;
        var point = this.getPointOnLineFromXValue(0);
        points[indexOfMin] = {x: 0, y: point.y};
    };

    this.extendPointsToMaximum = function () {
        var indexOfMax = (this.getMax("x") == points[0].x) ? 0 : 1;
        var point = this.getPointOnLineFromXValue(coordinateSystem.canvasWidth);
        points[indexOfMax] = {x: coordinateSystem.canvasWidth, y: point.y};
    };
}