
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
modules = {

    application {
        resource url: '/js/application.js'
    }

    slick {
        dependsOn 'application, jquery'
        resource url: '/js/slick/slick.js'
        resource url: '/css/slick/slick.css'
    }

    atmosphere {
        dependsOn 'application, jquery'
        resource url: '/js/atmosphere/jquery.atmosphere.js'
    }

    faq {
        dependsOn 'application, jquery'
        resource url: '/js/main/faq.js'
        resource url: '/css/faq/styles.css'
    }

    dropzone {
        dependsOn 'application, jquery'
        resource url: '/js/lib/dropzone.js'
    }

    kinetic {
        dependsOn 'application, jquery'
        resource url: '/js/kinetic/kinetic-v5.0.1.js'
    }

    thumbnail {
        dependsOn 'application, jquery'
        resource url: '/js/images/thumbnail.js'
    }

    wizardAdvanced {
        dependsOn 'application, jquery'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/selectAdvancedOptions.js'
    }

    wizardBackground {
        dependsOn 'application, jquery, dropzone, kinetic, slick'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/selectBackground.js'
    }

    wizardLocation {
        dependsOn 'application, jquery, kinetic'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/selectLocation.js'
        resource url: '/js/wizard/resizeObject.js'
    }

    wizardNumber {
        dependsOn 'application, jquery, kinetic'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/selectNumber.js'
        resource url: '/js/wizard/resizeObject.js'
    }

    wizardObject {
        dependsOn 'application, jquery, kinetic, slick'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/selectObject.js'
        resource url: '/js/wizard/resizeObject.js'
    }

    wizardJobSettings {
        dependsOn 'application, jquery'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/jobSettings.js'
    }

    wizardSummary {
        dependsOn 'application, jquery, thumbnail'
        resource url: '/js/wizard/wizard.js'
        resource url: '/js/wizard/summary.js'
    }

    objectModelCreate {
        dependsOn 'application, jquery, dropzone'
        resource url: '/js/objectModel/create.js'
    }

    jobShow {
        dependsOn 'application, jquery, atmosphere, thumbnail'
        resource url: '/js/job/show.js'
    }

    dcrList {
        dependsOn 'application, jquery'
        resource url: '/js/dynamicComputeResource/listInstances.js'
    }

    defineGeometryDrawing {
        dependsOn 'application, jquery, kinetic'
        resource url: '/js/coordinate/createGeometry.js'
        resource url: '/js/coordinate/insertScalingImage.js'
        resource url: '/js/coordinate/lineEquation.js'
    }

    tweenjs {
        dependsOn 'application' 
        resource url: 'js/lib/tween/tween.min.js'
    }

    threejs {
        resource url: 'js/lib/threejs/three.min.js'
        resource url: 'js/lib/threejs/stats.min.js'
        resource url: 'js/lib/threejs/controls/OrbitControlsZUp.js'
        resource url: 'js/lib/modelview/scenehandler.js'
        resource url: 'js/lib/tween/tween.min.js'
    }

    modelview {
        dependsOn 'application, jquery, threejs'
        resource url: 'js/lib/modelview/modelview.js'
        resource url: 'js/lib/modelview/modelrotatecontrols.js'
        resource url: 'css/modelview.css'
    }

    limitedaspects {
        dependsOn 'application, threejs, modelview, jquery-ui, jquery'
        resource url: 'js/lib/modelview/aspectHandler.js'
        resource url: 'css/limitedaspect.css'
    }

    groundPlaneControls {
        dependsOn 'application, jquery, jquery-ui, kinetic, threejs'
        resource url: '/js/wizard/wizard.js'
        // resource url: 'js/lib/modelview/groundControls.js'
        resource url: 'js/lib/modelview/threejsGroundPlane.js'
        resource url: '/js/wizard/resizeObject.js'
    }

    lightingcontrols {
        dependsOn 'application, threejs, modelview, jquery, jquery-ui'
        resource url: 'js/lib/modelview/lightingControls.js'
        resource url: 'css/lightingcontrols.css'
    }
}
