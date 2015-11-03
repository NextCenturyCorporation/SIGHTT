/**
 * Created with IntelliJ IDEA.
 * User: abovill
 * Date: 4/9/14
 * Time: 9:10 AM
 * To change this template use File | Settings | File Templates.
 */

var SceneConfig = function () {
    return {
        'showShadows': true,
        'rotationType': "SCENE",
        'showStatistics': false,
        'showAxes': true
    };
}

var SceneHandler = function (containerId, config) {
    this.container = document.getElementById(containerId);
    this.containerWidth = this.container.clientWidth;
    this.containerHeight = this.container.clientHeight;
    this.range = 30;

    //Renderer stuff
    this.renderer = new THREE.WebGLRenderer( {alpha: true });
    this.renderer.setSize(this.containerWidth, this.containerHeight);
    this.container.appendChild(this.renderer.domElement);
    // this.renderer.setClearColor(0xEEEEDD, 1.0);
    this.renderer.setClearColor( 0x000000, 0 ); // CHANGED


    //Scene stuff
    this.scene = new THREE.Scene();

    //camera stuff
    this.camera = new THREE.PerspectiveCamera(55, this.containerWidth / this.containerHeight, 1, 10000);
    // this.camera.position.set(this.range, -this.range / 25, this.range / 1.5 );
    this.camera.position.set( this.range / 10 , -this.range, this.range / 3 );
    this.camera.up.set(0, 0, 1);
    this.camera.lookAt(new THREE.Vector3(0, 0, 0));

    this.config = (config || new SceneConfig());
    this._init();


}

SceneHandler.prototype._init = function () {

    if (this.config['showAxes'] == true) {
        this.scene.add(new THREE.AxisHelper(10));
    }


    if (this.config['showStatistics'] == true) {
        this.stats = new Stats();
        this.stats.setMode(0);
        this.stats.domElement.style.position = 'absolute';
        this.stats.domElement.style.left = '0px';
        this.stats.domElement.style.top = '0px';
        this.container.appendChild(this.stats.domElement);
    }

    if (this.config['showShadows'] == true) {
        this.renderer.shadowMapEnabled = true;
        this.renderer.shadowMapSoft = true;
    } else {
        this.renderer.shadowMapEnabled = false;
        this.renderer.shadowMapSoft = false;
    }

    var scenehandler = this;
    window.addEventListener('resize', scenehandler.updateDimensions, false);

    this.controls = new THREE.OrbitControlsZUp(this.camera, this.container);
    if (this.config['rotationType'] == "SCENE") {
        // console.log("Rotating scene");
    } else {
        // console.log("Not rotating scene, someone else is handling rotations");
        this.controls.noRotate = true;
    }

    this.animate();
}

SceneHandler.prototype.updateDimensions = function (e) {
    this.containerWidth = this.container.clientWidth;
    this.containerHeight = this.container.clientHeight;
    this.renderer.setSize(this.containerWidth, this.containerHeight);
    this.camera.aspect = this.containerWidth / this.containerHeight;
    this.camera.updateProjectionMatrix();
}

SceneHandler.prototype.animate = function () {
    requestAnimationFrame(this.animate.bind(this));
    this.controls.update();
    if (this.config['showStatistics'] == true) {
        this.stats.update();
    }
    TWEEN.update(); //To help with TWEEN stuff...
    this.renderer.render(this.scene, this.camera);

};
