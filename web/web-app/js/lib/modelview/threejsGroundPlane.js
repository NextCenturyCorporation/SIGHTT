/**
 * Created with IntelliJ IDEA.
 * User: cdorman
 * Date: 5/19/14
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */

var WIDTH = 540;

function initializeGroundControls(container, backgroundWidth, backgroundHeight) {
    var groundControls = new GroundControls(container, backgroundWidth, backgroundHeight)
    initSliders(groundControls);
    return groundControls;
}


function initSliders(groundControls) {

    var gridInit = groundControls.getGridSize();
    var gridMin = 5;
    var gridMax = gridInit * 2;

    $('#sliderGridSize').slider({min: gridMin, max: gridMax, step: 1, value: gridInit, slide: function (event, ui) {
        handleSliders(groundControls, event, ui, "sliderGridSize");
    }});
    $("#gridSizeVal").text(gridInit);

}

function handleSliders(groundControls, event, ui, id) {
    var newVal = ui.value;

    if (id == "sliderGridSize") {
        $("#gridSizeVal").text(newVal);
        groundControls.resetGridSize(newVal)
    }

    groundControls.animate();
}

var GroundControls = function (passedContainer, backgroundWidth, backgroundHeight) {

    this.container = passedContainer;
    this.container.addEventListener('mousedown', onMouseDown, false);

    this.init(backgroundWidth, backgroundHeight);
    this.addThreeJSComponents();

    // ---------------------------------
    // Define the event listener functions here because it makes scope easier
    // ---------------------------------
    var scope = this;

    function onMouseDown(event) {
        event.preventDefault();

        // Keep track of whether or not the shift key was down when we got this event
        this.shiftDown = event.shiftKey;

        scope.container.addEventListener('mousemove', onMouseMove, false);
        scope.container.addEventListener('mouseup', onMouseUp, false);
        scope.container.addEventListener('mouseout', onMouseOut, false);

        scope.mouseXOnMouseDown = event.clientX - scope.windowHalfX;
        scope.targetXRotationOnMouseDown = scope.targetXRotation;
        scope.targetXLocationOnMouseDown = scope.group.position.x;

        scope.mouseYOnMouseDown = event.clientY - scope.windowHalfY;
        scope.targetYRotationOnMouseDown = scope.targetYRotation;
        scope.targetYLocationOnMouseDown = scope.group.position.y;
    }

    function onMouseUp(event) {
        scope.container.removeEventListener('mousemove', onMouseMove, false);
        scope.container.removeEventListener('mouseup', onMouseUp, false);
        scope.container.removeEventListener('mouseout', onMouseOut, false);
    }

    function onMouseOut(event) {
        scope.container.removeEventListener('mousemove', onMouseMove, false);
        scope.container.removeEventListener('mouseup', onMouseUp, false);
        scope.container.removeEventListener('mouseout', onMouseOut, false);
        scope.animate();
    }

    function onMouseMove(event) {
        var mouseX = event.clientX - scope.windowHalfX;
        var mouseY = event.clientY - scope.windowHalfY;

        if (this.shiftDown == true) {
            scope.group.position.x = scope.targetXLocationOnMouseDown + (mouseX - scope.mouseXOnMouseDown) *
                scope.scaleMouseToLocationSpeed;
            scope.group.position.y = scope.targetYLocationOnMouseDown + (-mouseY + scope.mouseYOnMouseDown) *
                scope.scaleMouseToLocationSpeed;
        }
        else {

            scope.targetXRotation = scope.targetXRotationOnMouseDown + ( mouseX - scope.mouseXOnMouseDown ) *
                scope.scaleMouseToRotationSpeed;


            scope.targetYRotation = scope.targetYRotationOnMouseDown + ( mouseY - scope.mouseYOnMouseDown ) *
                scope.scaleMouseToRotationSpeed;
        }
    }
};

GroundControls.prototype.init = function (backgroundWidth, backgroundHeight) {

    this.width = WIDTH;
    var aspectRatio = backgroundHeight / backgroundWidth;
    this.height = this.width * aspectRatio;

    // Variables based on changes in X
    this.targetXRotation = 0;
    this.targetXRotationOnMouseDown = 0;
    this.targetXLocationOnMouseDown = 0;
    this.mouseX = 0;
    this.mouseXOnMouseDown = 0;

    // Variables based on changes in Y
    this.targetYRotation = 0;
    this.targetYRotationOnMouseDown = 0;
    this.targetYLocationOnMouseDown = 0;
    this.mouseY = 0;
    this.mouseYOnMouseDown = 0;

    // Scale that controls how quickly changes in mouse X, Y get converted into rotation changes
    // this.rotationUpdateSpeed = 0.05;    // pretty slow, multiple renders before catches up
    this.rotationUpdateSpeed = 0.2;        // decent speed, 5 frames to change
    // this.rotationUpdateSpeed = 1.0;     // immediate, no feedback on effect of change

    // Scale that controls how much mouse movement gets converted to rotation change
    // this.scaleMouseToRotationSpeed = 0.001;     // too low, takes too much mouse movement to rotate
    this.scaleMouseToRotationSpeed = 0.005;     // good amount
    // this.scaleMouseToRotationSpeed = 0.02;     // overly sensitive

    this.scaleMouseToLocationSpeed = 0.54;


    this.windowHalfX = window.innerWidth / 2;
    this.windowHalfY = window.innerHeight / 2;

    this.gridSize = 160;

    this.shiftDown = false;
};

GroundControls.prototype.addThreeJSComponents = function () {
    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true  });
    this.renderer.setClearColor(0x000000, 0);
    this.renderer.setSize(this.getWidth(), this.getHeight());

    this.scene = new THREE.Scene();

    // var aspectRatio = 4 / 3;
    var aspectRatio = this.getWidth() / this.getHeight();
    var near = 1.0;
    var far = 10000.0;
    this.camera = new THREE.PerspectiveCamera(50, aspectRatio, near, far);
    this.camera.position.set(0, 0, 200);

    this.group = new THREE.Object3D();
    this.group.position.x = 0.0;
    this.group.position.y = 0.0;
    this.group.position.z = 0.0;
    this.scene.add(this.group);

    var ambientLight = new THREE.AmbientLight(0x404040);
    this.scene.add(ambientLight);

    this.plane = new THREE.Mesh(
        new THREE.PlaneGeometry(this.gridSize, this.gridSize, 20, 20),
        new THREE.MeshBasicMaterial({ color: 0x7f7f7f, wireframe: true }));
    this.group.add(this.plane);
};

GroundControls.prototype.resetGridSize = function (newSize) {
    this.gridSize = newSize;

    this.group.remove(this.plane);
    this.plane = new THREE.Mesh(
        new THREE.PlaneGeometry(this.gridSize, this.gridSize, 20, 20),
        new THREE.MeshBasicMaterial({ color: 0x7f7f7f, wireframe: true }));
    this.group.add(this.plane);

    this.animate();
}


GroundControls.prototype.animate = function () {
    requestAnimationFrame(this.animate.bind(this));

    // Make change over time (i.e. over multiple calls to render):
    this.group.rotation.y += ( this.targetXRotation - this.group.rotation.y ) * this.rotationUpdateSpeed;
    this.group.rotation.x += ( this.targetYRotation - this.group.rotation.x ) * this.rotationUpdateSpeed;

    this.renderer.render(this.scene, this.camera);
};

GroundControls.prototype.getRenderer = function () {
    return this.renderer;
};

GroundControls.prototype.getHeight = function () {
    return this.height;
};

GroundControls.prototype.getWidth = function () {
    return this.width;
};


GroundControls.prototype.getGridSize = function () {
    return this.gridSize;
};
