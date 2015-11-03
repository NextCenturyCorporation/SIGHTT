
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
package com.ncc.sightt;

import com.ncc.sightt.message.ImageType

class JobConfig {

    // Backgrounds and object models to use.  Note currently just 1 of each, but could be more later
    List backgrounds = []
    List objectModels = []

    // How many images and how do generate them.  If doAll==false, do numImages random images
    Integer numImages = 0
    Integer degreeSpacing = 0

    // Rendering information.  Where to render
    ModelLocation position = ModelLocation.CENTERED

    // PointsString is a JSON representation of the points that the user has selected
    String pointsString = ""
    Boolean customCameras = false
    String activeCamerasJSON = ""
    Boolean customLighting = false
    String lightingJSON = ""
    Boolean customGroundPlane = false
    String groundPlaneJSON = ""

    Double modelBackgroundScale = 1.0

    String imageType = ImageType.PNG

    Boolean reproducible = true
    Boolean generateAllMasks = false

    static hasMany = [backgrounds: Background, objectModels: ObjectModel]

    // This is necessary to allow pointsString to be >255 characters.
    static mapping = {
        pointsString type: "text"
        activeCamerasJSON type: "text"
        lightingJSON type: "text"
        groundPlaneJSON type: "text"
    }

}
