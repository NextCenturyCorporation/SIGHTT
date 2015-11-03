/**
 * Created by abovill on 12/23/13.
 */

modelview = null;

function log(msg) {
    if (window.console && window.console.log) {
        console.log(msg);
    }
}

var ModelView = function (containerId, config) {
    if (config == null) {
        log("Empty config in modelview.js, using default!");
        this.config = new SceneConfig();
    } else {
        this.config = config;
    }
    log("MODELVIEW CONFIG: ", this.config);
    this.scope = this;
    this.srcPath = "";
    this.containerId = containerId;

    this.sceneHandler = null;
    this.renderer = null;
    this.camera = null;
    this.scene = null;
    this.controls = null;
    this.container = null;

    this.object = null;
    this.objectMaterial = null;
    this.origin = new THREE.Vector3();

    //Color stuff
    this.objectColor = 0x7acf00;

    //UI Stuff
    this.progressBar = null;

    modelview = this;

    this.debugdata = {};

    this._init();
};

ModelView.prototype.setSrcPath = function (path) {
    this.srcPath = path;
}

ModelView.prototype._init = function () {
    this.sceneHandler = new SceneHandler(this.containerId, this.config);


    var outerContainer = document.getElementById("modelcontainer");
    if (outerContainer) {
        outerContainer.setAttribute("style", "width:" + this.containerWidth + "px");
    }
    this.progressBar = document.getElementById("progressbar");
    this.progressBar.style.position = 'relative';
    this.progressBar.style.top = '0px';
    this.progressBar.style.left = '0px';
    this.progressBar.style.backgroundColor = 'red';
    this.progressBar.style.padding = '5px 0px 5px 0px';
    this.progressBar.style.display = 'none';
    this.progressBar.style.overflow = 'visible';
    this.progressBar.style.whiteSpace = 'nowrap';
    this.progressBar.style.zIndex = 100;

    this.object = new THREE.Object3D();

    this.renderer = this.sceneHandler.renderer;
    this.camera = this.sceneHandler.camera;
    this.scene = this.sceneHandler.scene;
    this.container = this.sceneHandler.container;

    if (this.config['rotationType'] == "MODEL") {
        this.controls = new ModelRotateControls(this.object, this.sceneHandler.container);
    } else {
        this.controls = this.sceneHandler.controls;
    }


};

ModelView.prototype.loadModel = function (url) {
    log("Loading model: " + url);
    var modelWorker = this.newWorker("loadOBJ", url, function () {
        log("Callback of loadModel...");
    });
};

ModelView.prototype.updateCameraInfo = function () {
    //modelview.debugdata.cameraLocation = modelview.camera.position.toArray().toString();
}

ModelView.prototype.setCameraView = function (x, y, z) {
    modelview.camera.position = new THREE.Vector3(x, y, z);
    modelview.camera.lookAt(modelview.origin);
}

ModelView.prototype.animate = function () {
    requestAnimationFrame(modelview.animate);
    modelview.controls.update();
    modelview.stats.update();
    modelview.renderer.render(modelview.scene, modelview.camera);

};

/*
 * Event handlers below
 */
ModelView.prototype.newWorker = function (cmd, param, callback) {
    var worker = new Worker(this.srcPath + '/modelloader.js');
    worker.onmessage = function (event) {
        if (event.data.status == "complete") {
            modelview.geometry = new STLGeometry(event.data.content);
            modelview.loadObjectGeometry();
            modelview.progressBar.style.backgroundColor = 'lime';
            modelview.progressBar.style.width = "100%";
            modelview.progressBar.innerHTML = 'Model Loaded 100%';
            log("finished loading " + modelview.geometry.faces.length + " faces.");
            if (callback) {
                callback();
            }
        } else if (event.data.status == "progress") {
            modelview.progressBar.style.display = 'block';
            modelview.progressBar.style.width = event.data.content;
            modelview.progressBar.innerHTML = "Model Loading: " + event.data.content;
        } else if (event.data.status == "message") {
            log(event.data.content);
        } else if (event.data.status == "complete_points") {
            log("Complete_points happened apparently...");
        } else {
            log('Unknown Worker Message: ', event.data);
        }

    };

    worker.onerror = function (error) {
        error.preventDefault();
    };
    worker.postMessage({'cmd': cmd, 'param': param});
};

ModelView.prototype.loadObjectGeometry = function () {
    var material;
    if (this.scene && this.geometry) {
        if (this.objectMaterial == 'wireframe') {
            log("WIREFRAME MODEL: ", this.objectColor);
            material = new THREE.MeshBasicMaterial({ambient: this.objectColor, color: this.objectColor, wireframe: true});
        } else {
            log("LAMBERT MODEL: ", this.objectColor);
            material = new THREE.MeshLambertMaterial({ambient: this.objectColor, color: this.objectColor, shading: THREE.SmoothShading});
        }

        // scene.removeObject(this.object);

        if (this.object) {
            // shouldn't be needed, but this fixes a bug with webgl not removing previous object when loading a new one dynamically
            this.object.materials = [new THREE.MeshBasicMaterial({color: 0xffffff, opacity: 0})];
            this.scene.remove(this.object);
        }

        var tmpObj = new THREE.Mesh(this.geometry, material.clone());
        this.object.add(tmpObj);
        if (this.config['showShadows'] == true) {
            tmpObj.castShadow = true;
            tmpObj.receiveShadow = true;
        } else {
            tmpObj.castShadow = false;
            tmpObj.receiveShadow = false;
        }

        this.scene.add(this.object);

        if (this.objectMaterial != 'wireframe') {
            this.object.overdraw = true;
            this.object.doubleSided = true;
        }

        this.object.updateMatrix();
    }
}

var STLGeometry = function (stlArray) {
    THREE.Geometry.call(this);

    var scope = this;

    for (var i = 0; i < stlArray[0].length; i++) {
        v(stlArray[0][i][0], stlArray[0][i][1], stlArray[0][i][2]);
    }

    for (var i = 0; i < stlArray[1].length; i++) {
        f3(stlArray[1][i][0], stlArray[1][i][1], stlArray[1][i][2]);
    }

    function v(x, y, z) {
        scope.vertices.push(new THREE.Vector3(x, y, z));
    }

    function f3(a, b, c) {
	scope.faces.push(new THREE.Face3(a, b, c));
    }

    this.computeCentroids();
    this.computeFaceNormals();

    scope.min_x = 0;
    scope.min_y = 0;
    scope.min_z = 0;

    scope.max_x = 0;
    scope.max_y = 0;
    scope.max_z = 0;

    for (var v = 0, vl = scope.vertices.length; v < vl; v++) {
        scope.max_x = Math.max(scope.max_x, scope.vertices[v].x);
        scope.max_y = Math.max(scope.max_y, scope.vertices[v].y);
        scope.max_z = Math.max(scope.max_z, scope.vertices[v].z);

        scope.min_x = Math.min(scope.min_x, scope.vertices[v].x);
        scope.min_y = Math.min(scope.min_y, scope.vertices[v].y);
        scope.min_z = Math.min(scope.min_z, scope.vertices[v].z);
    }

    scope.center_x = (scope.max_x + scope.min_x) / 2;
    scope.center_y = (scope.max_y + scope.min_y) / 2;
    scope.center_z = (scope.max_z + scope.min_z) / 2;

    for (var v = 0, vl = scope.vertices.length; v < vl; v++) {
        scope.vertices[v].x -= scope.center_x;
        scope.vertices[v].y -= scope.center_y;
    }
    scope.max_x -= scope.center_x;
    scope.max_y -= scope.center_y;

    scope.min_x -= scope.center_x;
    scope.min_y -= scope.center_y;

    scope.center_x = 0;
    scope.center_y = 0;
};

STLGeometry.prototype = new THREE.Geometry();
STLGeometry.prototype.constructor = STLGeometry;

/*
 This function is for drawing 2D annotations on the canvas...
 */
function toXYCoords(pos) {
    var vector = projector.projectVector(pos.clone(), camera);
    vector.x = (vector.x + 1) / 2 * window.innerWidth;
    vector.y = -(vector.y - 1) / 2 * window.innerHeight;
    return vector;
}

ModelView.prototype.drawDebugText = function () {
    var tbl = document.createElement("table");
    var tblBody = document.createElement("tbody");
    for (var key in modelview.debugdata) {

        var tr = document.createElement("tr");
        var tdKey = document.createElement("td");
        var tdVal = document.createElement("td");
        tdKey.appendChild(document.createTextNode(key));
        tdVal.appendChild(document.createTextNode(modelview.debugdata[key]));
        tr.appendChild(tdKey);
        tr.appendChild(tdVal);
        tblBody.appendChild(tr);
    }
    tbl.appendChild(tblBody);
    var text2 = document.getElementById('debuginfo');
    if (!!text2) {
        while (text2.firstChild) {
            text2.removeChild(text2.firstChild);
        }

        text2.appendChild(tbl);

    }


};

function radToDeg(rad) {
    return (rad * (180 / Math.PI)).toFixed(2);
}


