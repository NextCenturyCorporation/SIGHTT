
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
package com.ncc.sightt.message

import java.util.List;

/**
 * Contains the information necessary to render an object through blender and
 * composite it into a background image.
 */
class BaseRenderMessage extends BaseMessage {
    static long serialVersionUID = 5L

    // Input data, going from web TaskService to TaskConsumer
    Long taskId
    String bucketName
    String backgroundName
    String backgroundKey
    String modelName
    String modelKey

    // NOTE: these values are set to default values so that
    // ModelRenderMessage does not need to set them.
    Double scaleFactor = 1.0
    Float yaw = 0
    Float pitch = 0
    Float roll = 0

    //Lighting
    Boolean useLightingModel = false
    String sunLocation ="(5,0,5)"
    Double sunIntensity = 5.0
    String sunColor = "(1.0,1.0,1.0)"
    Boolean useAmbient = true
    Double ambientIntensity = 0.25
    Integer ambientSamples = 10

    //GroundPlane
    Boolean useGroundPlaneModel = false
    Double groundPositionX = 0.0
    Double groundPositionY = 0.0
    Double groundPositionZ = 0.0
    Double groundRotationX = 0.0
    Double groundRotationY = 0.0

    String imageType = ImageType.PNG
    Boolean generateAllMasks = false
    Boolean generateObjectFile = false

    Integer pointX
    Integer pointY

    // Results from the TaskConsumer
    String compositeImageFilename
    String maskImageFilename
    List<String> partMaskImageFilenames
    String objFilename
    String compositeKey
    String compositeThumbKey
    String maskKey
    List<String> partMaskKeys
    String objFileKey
    String metadata
    String stderr
    String stdout
    Integer exitValue

    // Start is when processing starts at task consumer, end is when it finishes
    Long executionTimeStart
    Long executionTimeEnd
}
