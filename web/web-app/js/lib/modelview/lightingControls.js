/**
 * Created with IntelliJ IDEA.
 * User: abovill
 * Date: 4/8/14
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */

function initializeLightingControls(modelView) {

    var lightingControls = new LightingControls();
    lightingControls.init(modelView);

    //Load cameras asynchronously!
    initSliders(lightingControls);
    var x = $('#azimuthSlider').slider("value");
    var y = $('#elevationSlider').slider("value");
    var d = $("#intensityD").slider("value");
    var a = $("#intensityA").slider("value");
    lightingControls.updateLight(x, y);
    $('#xval').text(x.toFixed(3) + " (" + radToDeg(x) + " deg)");
    $('#yval').text(y.toFixed(3) + " (" + radToDeg(y) + " deg)");

    $('#dval').text(d);
    $('#aval').text(a);
    lightingControls.updateIntensity(d, a);
}


function initSliders(lightingControls) {

    var initElevation = degToRad( 75 );
    var initAzimuth = degToRad( -110 );

    $('#elevationSlider').slider({min: (0), max: (Math.PI / 2), step: (Math.PI / 100), value: initElevation, slide: function (event, ui) {
        handleSliders(lightingControls, event, ui, "phi");
    }});
    $('#azimuthSlider').slider({min: (-1 * Math.PI), max: Math.PI, step: (Math.PI / 100), value: initAzimuth, slide: function (event, ui) {
        handleSliders(lightingControls, event, ui, "theta");
    }});
    $('#intensityD').slider({min: 0, max: 1, step: 0.01, value: 0.5, slide: function (event, ui) {
        handleIntensity(lightingControls, event, ui, "directional");
    }});
    $('#intensityA').slider({min: 0, max: 1, step: 0.01, value: 0.25, slide: function (event, ui) {
        handleIntensity(lightingControls, event, ui, "ambient");
    }});
}

function radToDeg(rad) {
    return (rad * (180 / Math.PI)).toFixed(1);
}

function degToRad(deg) { 
    return ((deg * Math.PI)/180);
}


function handleSliders(lightingControls, event, ui, id) {
    var newVal = ui.value;
    var degVal = radToDeg(newVal);
    var theta, phi;
    if (id == "theta") {
        $("#xval").text(newVal.toFixed(3) +  " (" + degVal + " deg)");
        theta = newVal;
        phi = $('#elevationSlider').slider("value");
    } else {
        $("#yval").text(newVal.toFixed(3) +  " (" + degVal + " deg)");
        theta = $('#azimuthSlider').slider("value");
        phi = newVal;
    }

    lightingControls.updateLight(theta, phi);
}

function handleIntensity(lightingControls, event, ui, id) {
    var newVal = ui.value;
    var dir, amb;
    if (id == "directional") {
        $('#dval').text(newVal);
        dir = newVal;
        amb = $('#intensityA').slider("value");
    } else {
        $('#aval').text(newVal);
        dir = $('#intensityD').slider("value");
        amb = newVal;
    }
    lightingControls.updateIntensity(dir, amb);
    // $('#aval').text(lightingControl.ambientLight.color.getHexString());
}

LightingControls = function () {
    this.domeMesh = null;
    this.directionalLight = null;
    this.lightOrb = null;
}

LightingControls.prototype.init = function (modelview) {
    this.modelview = modelview;
    this.modelview.doShadows = true;

    // The dome adds a hemisphere over the object. Not really needed
//    var geometry = new THREE.SphereGeometry(15, 100, 100, 0, Math.PI, 2 * Math.PI);
//    var material = new THREE.MeshBasicMaterial({ color: 0xddddff, transparent: true, opacity: 0.25});
//    this.domeMesh = new THREE.Mesh(geometry, material);
//    this.domeMesh.material.side = THREE.DoubleSide;
//    this.modelview.scene.add(this.domeMesh);

    var geometry = new THREE.SphereGeometry(1, 100, 100);
    var material = new THREE.MeshBasicMaterial({color: 0xFFFF00, transparent: true, opacity: 0.75});
    this.lightOrb = new THREE.Mesh(geometry, material);
    this.lightOrb.position = new THREE.Vector3(0, 0, 15);
    this.modelview.scene.add(this.lightOrb);

    this.directionalLight = new THREE.DirectionalLight(0xffffff);
    this.directionalLight.target = this.modelview.object;
    this.directionalLight.intensity = 0.5;
    this.directionalLight.castShadow = true;

    this.directionalLight.shadowCameraNear = 1;
    this.directionalLight.shadowCameraFar = 30;

    this.directionalLight.shadowCameraRight = 10;
    this.directionalLight.shadowCameraLeft = -10;
    this.directionalLight.shadowCameraTop = 10;
    this.directionalLight.shadowCameraBottom = -10;

    this.directionalLight.shadowCameraVisible = false;

    this.directionalLight.position.set(this.lightOrb.position);
    this.modelview.scene.add(this.directionalLight);

    var floorMaterial = new THREE.MeshPhongMaterial({ color: 0xe8e8e8});
    var floorGeometry = new THREE.PlaneGeometry(20, 20, 20, 20);
    this.floor = new THREE.Mesh(floorGeometry, floorMaterial);
    this.floor.position.set(0, 0, -1.5);
    this.floor.receiveShadow = true;
    this.modelview.scene.add(this.floor);

    this.ambientLightColor = new THREE.Color(0x000000);
    this.maxAmbientColor = new THREE.Color(0xffffff);
    this.ambientLight = new THREE.AmbientLight(this.ambientLightColor);
    this.modelview.scene.add(this.ambientLight);


}

LightingControls.prototype.updateLight = function (theta, phi) {
    /*    var newX = Math.cos(phi) * Math.cos(theta);
     var newY = Math.cos(phi) * Math.sin(theta);
     var newZ = Math.sin(phi);*/
    var newXYZ = thetaPhiToXYZ(theta, phi);
    var lightLocation = (new THREE.Vector3(newXYZ.x, newXYZ.y, newXYZ.z)).normalize();
    var orbLocation = new THREE.Vector3();
    orbLocation.copy(lightLocation);
    orbLocation.setLength(15);
//    this.directionalLight.position.set(lightLocation.x, lightLocation.y, lightLocation.z);
    this.directionalLight.position = orbLocation.clone();
    this.lightOrb.position = orbLocation;
//    this.lightOrb.needsUpdate = true;
//    this.directionalLight.needsUpdate = true;
//    this.floor.needsUpdate = true;
};

LightingControls.prototype.updateIntensity = function (directional, ambient) {
    this.directionalLight.intensity = directional;
    this.directionalLight.shadowDarkness = directional;
    this.ambientLightColor.copy(this.maxAmbientColor);
    this.ambientLightColor.multiplyScalar(ambient);
    this.ambientLight.color = this.ambientLightColor;
    this.directionalLight.needsUpdate = true;
    this.ambientLight.needsUpdate = true;
    this.modelview.object.needsUpdate = true;

};

function thetaPhiToXYZ(theta, phi) {
    var x = Math.cos(phi) * Math.cos(theta);
    var y = Math.cos(phi) * Math.sin(theta);
    var z = Math.sin(phi);
    return {x: x, y: y, z: z};
}
