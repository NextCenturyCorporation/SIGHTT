/**
 * Load cameras and model,create the overall
 */

aspecthandler = null;

var scaleFactor = 1;
var sphereSize = 0.2 * scaleFactor;
var camDistMultiplier;

var polyhedronScale = 13;
var camGeometry = new THREE.SphereGeometry(sphereSize, 10);
var camMaterial = new THREE.MeshBasicMaterial({color: 0xFFFFFF, wireframe: false});
var lineMaterial = new THREE.LineBasicMaterial({color: 0xBBBBBB});

AspectHandler = function () {
    aspecthandler = this;
    this.cameraObjects = null;

    //The voronoi tessella
    this.tessella = null;

    //The vertices from ALL voronoi tessella in the tessellation
    this.vertices = null;

    //The vertices (by id) that make up the THREE.Face3 objects in the polyhedron
    this.faces = null;

    //These are the forward and reverse mappings between faces and tessella
    this.tessellaToFaces = null;
    this.faceToTessella = null;

    //This is the polyhedron
    this.polyhedron = null;
    this.voronoiMesh = null;
    this.faceOutlines = null;

    //contains the cameraOrbs AND extra data...
    this.cameraObjects = null;
    this.cameraOrbs = null;

    this.modelview = null;
    this.projector = new THREE.Projector();
    this.mouseVector = new THREE.Vector3();

    //Materials for different face states
    this.materials = null;
    this.neighborLevel = 0;

    camDistMultiplier = 1.1;

    /*
     * This is the aspects map, it contains all of the information about the aspects being displayed (including dirty status and active/disabled status
     */
    this.aspects = {};
    this.dirtyList = null;
    this.faceSides = {};
    this.cameraAspectInfo = null;
    this.enableHandlers = true;
    this.initialized = false;
}

AspectHandler.prototype.addAspectsToModelview = function () {
    this.modelview.scene.add()
};

AspectHandler.prototype.init = function (modelview, cameraAspectInfo) {
    this.cameraObjects = new Array();
    this.tessella = new Array();
    this.vertices = new Array();
    this.faces = new Array();
    this.tessellaToFaces = {};
    this.faceToTessella = {};
    this.dirtyList = new Array();
    this.lastHighlightedTessella = null;
    this.faceOutlines = new THREE.Object3D();
    this.cameraAspectInfo = cameraAspectInfo;

    scaleFactor = Math.min(1.0 / (Math.log(this.cameraAspectInfo.cameras.length - 1) / Math.log(10)), 1.0);

    this.baseMaterial = new THREE.MeshBasicMaterial({color: 0xFFFFFF, vertexColors: THREE.FaceColors, transparent: true, opacity: 0.15, wireframe: false});

    this.faceColors = {
        'active': new THREE.Color(0x40FF40),
        'inactive': new THREE.Color(0xFF4040),
        'highlighted': new THREE.Color(0x8888FF),
        'camActive': new THREE.Color(0x059905),
        'camInactive': new THREE.Color(0xFF4040),
        'camHighlighted': new THREE.Color(0x8888FF)
    };

//Load the camera orbs
    this.loadCameraOrbs();

//Load the voronoi diagram
    this.loadCameraFaces();

    this.loadFaceGroupings();

//hook in the modelview (we'll need it to update the voronoi)
    this.sceneHandler = modelview.sceneHandler;

    this.generateAspectsStructure();


    if (this.enableHandlers == true) {
        this.sceneHandler.container.addEventListener('mousedown', this.selectCameras, false);
        this.sceneHandler.container.addEventListener('mousemove', this.hoverHighlight, false);
    }
    this.sceneHandler.container.addEventListener('updateDirtyCameras', this.updateDirtyCameras, false);

    this.addVisualizationToModelView();
    var event = new Event('updateDirtyCameras');
    aspecthandler.sceneHandler.container.dispatchEvent(event);
    this.initialized = true;
}

AspectHandler.prototype.generateAspectsStructure = function () {
    for (var idx = 0; idx < this.tessella.length; idx++) {
        this.aspects[idx] = {active: true}
        this.dirtyList.push(idx);

    }
};

/**
 * Recursively determine n-levels of neighbors for the given list of rootNodes. Nodes in previousRoots will not be added to the list.
 * @param rootNodes array of nodes (in the form {tessellaId: id})
 * @param previousRoots
 * @param neighborLevels
 * @returns {Array}
 * @private
 */
AspectHandler.prototype._findNeighborsOfFace = function (rootNodes, previousRoots, neighborLevels) {
    var neighborIndexes = new Array();
    var neighborTessella = new Array();
    var scope = this;
    if (neighborLevels < 1) {
        return neighborTessella;
    } else {
        rootNodes.forEach(function (rootTessellaId, index, array) {
            var commonVertex = aspecthandler.tessella[rootTessellaId].index;
            /*
             * Search for delaunay triangles that share the vertex at the center-point of the tessella
             */
            scope.cameraAspectInfo.triangles.forEach(function (triangle) {
                if (triangle.indexOf(commonVertex) >= 0) {
                    //triangle contains the common vertex
                    triangle.forEach(function (idx) {
                        if (neighborIndexes.indexOf(idx) < 0) {
                            neighborIndexes.push(idx);
                        }
                    });

                }
            });
            /*
             * For each of the neighbor vertices found above, translate them to the tessellaId of the face that has them
             * as the center-point
             */
            aspecthandler.tessella.forEach(function (tessella, index, array) {
                var tIdx = tessella.index;
                if (neighborIndexes.indexOf(tIdx) >= 0) {
                    if (rootNodes.indexOf(tIdx) < 0 && previousRoots.indexOf(tIdx) < 0 && neighborTessella.indexOf(tIdx) < 0) {
                        neighborTessella.push(tIdx);
                    }
                }
            });
        });

        var prevJoined = rootNodes;
        prevJoined.concat(previousRoots);
        return neighborTessella.concat(this._findNeighborsOfFace(neighborTessella, prevJoined, neighborLevels - 1));
    }

}

/**
 * Load the cameras into groupings for front, back, top, bottom, left, right
 */
AspectHandler.prototype.loadFaceGroupings = function () {
    this.faceSides = {'front': new Array(), 'back': new Array(), 'left': new Array(), 'right': new Array(), 'top': new Array(), 'bottom': new Array()}

    var sides = this.faceSides;
    var numCams = this.cameraAspectInfo.cameras.length;
    var cameras = this.cameraAspectInfo.cameras;
    //Find the *most faces (orbs)
    for (var idx = 0; idx < numCams; idx++) {
        var cam = this.cameraAspectInfo.cameras[idx];
        //Front is the front of the object (the grill of a truck, the rocket of an RPG)
        if (sides['front'].length == 0 || cam.y > cameras[sides['front'][0]].y) {
            sides['front'][0] = idx;
        }
        if (sides['back'].length == 0 || cam.y < cameras[sides['back'][0]].y) {
            sides['back'][0] = idx;
        }
        //Left is the Driver's side of the object (in the US)
        if (sides['left'].length == 0 || cam.x < cameras[sides['left'][0]].x) {
            sides['left'][0] = idx;
        }
        if (sides['right'].length == 0 || cam.x > cameras[sides['right'][0]].x) {
            sides['right'][0] = idx;
        }
        if (sides['top'].length == 0 || cam.z > cameras[sides['top'][0]].z) {
            sides['top'][0] = idx;
        }
        if (sides['bottom'].length == 0 || cam.z < cameras[sides['bottom'][0]].z) {
            sides['bottom'][0] = idx;
        }
    }

    /*
     * Locate the 1st order neighbors.
     */
    for (key in sides) {
        var neighborTessella = this._findNeighborsOfFace(sides[key], [], this.neighborLevel);
        sides[key] = sides[key].concat(neighborTessella);
    }
}

AspectHandler.prototype.addVisualizationToModelView = function () {
    aspecthandler.sceneHandler.scene.add(this.voronoiMesh);
    aspecthandler.sceneHandler.scene.add(this.cameraOrbs);
    aspecthandler.sceneHandler.scene.add(this.faceOutlines);
}

AspectHandler.prototype.loadCameraOrbs = function () {
    this.cameraAspectInfo.cameras.forEach(this.loadCameraOrbData);

    this.cameraOrbs = new THREE.Object3D();

    this.cameraObjects.forEach(function (element, index, array) {
        var cam = element.cam.clone();
        aspecthandler.cameraOrbs.add(cam);
    });
};

AspectHandler.prototype.loadCameraFaces = function () {
    this.cameraAspectInfo.tessella.forEach(this.loadTessellaData);
    this.polyhedron = new THREE.PolyhedronGeometry(this.vertices, this.faces, polyhedronScale);

    this.voronoiMesh = new THREE.Mesh(this.polyhedron, this.baseMaterial);
};

AspectHandler.prototype.findIntersects = function (clickX, clickY) {
    var scrollTopOffset = $(window).scrollTop();
    var scrollLeftOffset = $(window).scrollLeft();
    var widthOffset = this.sceneHandler.container.offsetLeft;
    var heightOffset = this.sceneHandler.container.offsetTop;
    aspecthandler.mouseVector.x = 2 * ((clickX - widthOffset + scrollLeftOffset) / this.sceneHandler.containerWidth) - 1;
    aspecthandler.mouseVector.y = 1 - 2 * ( (clickY - heightOffset + scrollTopOffset) / this.sceneHandler.containerHeight );

    var intersects = [];
    if (!!aspecthandler.voronoiMesh) {
        var raycaster = aspecthandler.projector.pickingRay(aspecthandler.mouseVector.clone(), aspecthandler.sceneHandler.camera);
        intersects = raycaster.intersectObject(aspecthandler.voronoiMesh, true);
    }

    return intersects;
};

AspectHandler.prototype.updateDirtyCameras = function (event) {
    var delay = event.detail || null;
    while (aspecthandler.dirtyList.length > 0) {
        var tessellaId = aspecthandler.dirtyList.pop();
        if (aspecthandler.aspects[tessellaId].active == true) {
            aspecthandler.colorTessella(tessellaId, 'active', delay);
            aspecthandler.colorCamera(tessellaId, 'camActive', delay);
        } else {
            aspecthandler.colorTessella(tessellaId, 'inactive', delay);
            aspecthandler.colorCamera(tessellaId, 'camInactive', delay);
        }
    }
};

AspectHandler.prototype.hoverHighlight = function (mouseEvent) {
    var intersection = aspecthandler.findIntersects(mouseEvent.clientX, mouseEvent.clientY);
    var event = new Event('updateDirtyCameras');
    aspecthandler.sceneHandler.container.dispatchEvent(event);
    if (intersection.length > 0) {
        var tessellaId = aspecthandler.faceToTessella[intersection[0].faceIndex];
        if (aspecthandler.lastHighlightedTessella != tessellaId) {
            if (aspecthandler.lastHighlightedTessella != null) {
                aspecthandler.dirtyList.push(aspecthandler.lastHighlightedTessella);
            }
            aspecthandler.lastHighlightedTessella = tessellaId;
            aspecthandler.colorTessella(tessellaId, 'highlighted');
            aspecthandler.colorCamera(tessellaId, 'camHighlighted');
        }

    } else {
        if (aspectHandler.lastHighlightedTessella != null) {
            aspecthandler.dirtyList.push(aspectHandler.lastHighlightedTessella);
            aspecthandler.lastHighlightedTessella = null;
        }
    }

};

AspectHandler.prototype.selectCameras = function (e) {

    var intersection = aspecthandler.findIntersects(e.clientX, e.clientY);
    if (intersection.length > 0) {
        var obj = intersection[0].object;
        var dirtyTessella = aspecthandler.faceToTessella[intersection[0].faceIndex];
        aspecthandler.aspects[dirtyTessella].active = !aspecthandler.aspects[dirtyTessella].active;
        aspecthandler.dirtyList.push(dirtyTessella);
        $(document).trigger("modified-aspects");
    }
    var event = new Event('updateDirtyCameras');
    aspecthandler.sceneHandler.container.dispatchEvent(event);
};

AspectHandler.prototype.getTessellaFromFace = function (face) {

};

AspectHandler.prototype.getFacesForTessella = function (tessella) {

};

/**
 * Generates the camera orbs for each of the given camera locations
 * @param theView
 * @param locations
 */
AspectHandler.prototype.loadCameraOrbData = function (element, index, array) {
    var cameraMesh = new THREE.Mesh(camGeometry, camMaterial.clone());
    cameraMesh.autoUpdateMatrix = false;
    var camPosition = new THREE.Vector3(element.x, element.y, element.z);
    camPosition.multiplyScalar(camDistMultiplier);
    cameraMesh.position = (camPosition);
    cameraMesh.needsUpdate = true

    aspecthandler.cameraObjects.push({'cam': cameraMesh, 'thetaPhi': {'theta': element.theta, 'phi': element.phi}});
}

/**
 * Generates the THREE.Face3 objects for each tessella and then generates two maps:
 * The first map maps each tessella to its corresponding THREE.Face3 objects
 * The second map maps each THREE.Face3 object to the tessella it belongs to.
 * @param element
 * @param index
 * @param array
 */
AspectHandler.prototype.loadTessellaData = function (element, index, array) {
    var numVerticesInTessella = element.vertexes.length;
    var currentTessellaIndex = aspecthandler.tessella.length;
    aspecthandler.tessella.push(element);

    var baseVertexIndex = aspecthandler.vertices.length;
    var outlineGeometry = new THREE.Geometry();
    var startingVertex = null;
    element.vertexes.forEach(function (vertex, index, array) {
        aspecthandler.vertices.push([vertex.vertexPoint.x, vertex.vertexPoint.y, vertex.vertexPoint.z]);
        var vertexForWireframe = new THREE.Vector3(vertex.vertexPoint.x, vertex.vertexPoint.y, vertex.vertexPoint.z);
        vertexForWireframe.multiplyScalar(polyhedronScale);
        if (startingVertex == null) {
            startingVertex = vertexForWireframe;
        }
        outlineGeometry.vertices.push(vertexForWireframe);
    });
    outlineGeometry.vertices.push(startingVertex);
    var faceOutline = new THREE.Line(outlineGeometry, lineMaterial.clone());
    aspecthandler.faceOutlines.add(faceOutline);

    /*
     * Now generate the faces using the stored pointers and whatnot.
     */
    var facesOfTessella = new Array();

    var vertexBaseOffset;
    for (vertexBaseOffset = 0; vertexBaseOffset < numVerticesInTessella - 2; vertexBaseOffset++) {
        var faceVertices = new Array();
        //Create a list of triangles that make up the face...
        var vertexIndices = [baseVertexIndex, (baseVertexIndex + (1 + vertexBaseOffset)), (baseVertexIndex + (2 + vertexBaseOffset))];

        faceVertices.push(vertexIndices[0]);
        faceVertices.push(vertexIndices[1]);
        faceVertices.push(vertexIndices[2]);
        var currentFaceIndex = aspecthandler.faces.length;
        facesOfTessella.push(currentFaceIndex);
        //map face to tessella (BEFORE pushing, so we can just use length)
        aspecthandler.faceToTessella[currentFaceIndex] = currentTessellaIndex;
        aspecthandler.faces.push(faceVertices);
    }
    aspecthandler.tessellaToFaces[currentTessellaIndex] = facesOfTessella;
}

AspectHandler.prototype.colorTessella = function (tessellaId, colorName, speed) {
    var transitionSpeed = speed || 50;

    var clonedColor = aspecthandler.faceColors[colorName].clone();
    var color = aspecthandler.polyhedron.faces[this.tessellaToFaces[tessellaId][0]].color.clone();
    var that = this;
    var tween = new TWEEN.Tween(color).to(clonedColor, transitionSpeed).onUpdate(function () {
        that.tessellaToFaces[tessellaId].forEach(function (element, index, array) {
            var face = aspecthandler.polyhedron.faces[element];
            face.color = color;
        });
        aspecthandler.polyhedron.colorsNeedUpdate = true;
    });
    tween.start();

};

AspectHandler.prototype.colorCamera = function (cameraId, colorName) {
    aspecthandler.cameraOrbs.children[cameraId].material.color = aspecthandler.faceColors[colorName].clone();
}

function randomlyColorTessella(geom, tessellaMap) {
    for (var key in tessellaMap) {
        var color = new THREE.Color(Math.floor(Math.random() * 16777215));
        colorTessella(key, color);
    }
}

AspectHandler.prototype.colorPolyhedron = function (polyhedron, tessellaMap) {
    for (var key in tessellaMap) {
        var color = new THREE.Color(aspecthandler.activeFaceColor);
        colorTessella(key, color);
    }

};

/**
 * Currently only toggles a single tessella per side
 * @param side
 */
AspectHandler.prototype.toggleFaces = function (side, state) {
    if (this.initialized == false) {
        return;
    }
    if (side == "all") {
        for (var tessellaId in aspecthandler.aspects) {
            if (state == null) {
                aspecthandler.aspects[tessellaId].active = !aspecthandler.aspects[tessellaId].active;
            } else {
                aspecthandler.aspects[tessellaId].active = state;
            }
            aspecthandler.dirtyList.push(tessellaId);
        }
    } else {
        this.faceSides[side].forEach(function (tessellaId, index, array) {
            if (state == null) {
                aspecthandler.aspects[tessellaId].active = !aspecthandler.aspects[tessellaId].active;
            } else {
                aspecthandler.aspects[tessellaId].active = state;
            }

            aspecthandler.dirtyList.push(tessellaId);
        });
    }

    var event = new CustomEvent('updateDirtyCameras', {'detail': 300});

    aspecthandler.sceneHandler.container.dispatchEvent(event);
    $(document).trigger("modified-aspects");

}
