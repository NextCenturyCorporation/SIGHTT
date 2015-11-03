/**
 * Created with IntelliJ IDEA.
 * User: abovill
 * Date: 5/19/14
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */

"use strict";
var ModelRotateControls = function (object, domElement) {
    var scope = this;
    this.object = object;
    this.domElement = ( domElement !== undefined ) ? domElement : document;
    this.enabled = true;


    // How far you can orbit vertically, upper and lower limits.
    // Range is 0 to Math.PI radians.
    this.minPolarAngle = 0; // radians
    this.maxPolarAngle = Math.PI; // radians
    this.rotateSpeed = 1.0;

    var rotateStart = new THREE.Vector2();
    var rotateEnd = new THREE.Vector2();
    var rotateDelta = new THREE.Vector2();
    var phiDelta = 0;
    var thetaDelta = 0;

    this.domElement.addEventListener('contextmenu', function (event) {
        event.preventDefault();
    }, false);
    this.domElement.addEventListener('mousedown', onMouseDown, false);

    this.update = function () {
        this.object.rotation.z += thetaDelta;
        this.object.rotation.y += phiDelta;
        thetaDelta = 0;
        phiDelta = 0;
    }

    this.rotateLeft = function (angle) {

        thetaDelta -= angle;

    };

    this.rotateUp = function (angle) {

        phiDelta -= angle;

    };

    function onMouseDown(event) {
        rotateStart.set(event.clientX, event.clientY);

        scope.domElement.addEventListener('mousemove', onMouseMove, false);
        scope.domElement.addEventListener('mouseup', onMouseUp, false);
    }

    function onMouseMove(event) {
        event.preventDefault();
        var element = scope.domElement === document ? scope.domElement.body : scope.domElement;


        rotateEnd.set(event.clientX, event.clientY);
        rotateDelta.subVectors(rotateEnd, rotateStart);

        // rotating across whole screen goes 360 degrees around
        scope.rotateLeft(2 * Math.PI * rotateDelta.x / element.clientWidth * scope.rotateSpeed);
        // rotating up and down along whole screen attempts to go 360, but limited to 180
        scope.rotateUp(2 * Math.PI * rotateDelta.y / element.clientHeight * scope.rotateSpeed);

        rotateStart.copy(rotateEnd);

        scope.update();

    }

    function onMouseUp(event) {
        scope.domElement.removeEventListener('mousemove', onMouseMove, false);
        scope.domElement.removeEventListener('mouseup', onMouseUp, false);
    }
};
ModelRotateControls.prototype = new THREE.EventDispatcher();