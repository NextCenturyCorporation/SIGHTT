
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
package com.ncc.sightt

class JobTask {

    // This task's index for its Job.  Each job will have tasks numbered 1, 2, 3, etc.
    // Note that counting starts at 1, not 0.
    // This value is necessarily different than JobTask.id, which is a global id number
    // as determined by the database.
    Integer taskNumber = 0

    // Background and object model info
    Background background
    ObjectModel objectModel

    // Rotations of the object model for rendering
    Float yaw
    Float pitch
    Float roll

    // Rendering information: centered in the background and size
    Double scaleFactor
    Integer locationX
    Integer locationY

    // Resulting composite
    CompositeImage compositeImage

    //Lighting Information
    Boolean useLightingModel = false
    String sunLocation = "(5, 0, 5)"
    Double sunIntensity = 5.0
    String sunColor = "(1.0,1.0,1.0)"
    Boolean useAmbient = true
    Double ambientIntensity = 0.25
    Integer ambientSamples = 10

    //GroundPlane Information
    Boolean useGroundPlaneModel = false
    Double groundPlanePositionX = 0.0
    Double groundPlanePositionY = 0.0
    Double groundPlanePositionZ = 0.0
    Double groundPlaneRotationX = 0.0
    Double groundPlaneRotationY = 0.0

    // taskTimeStart is the time that the task was created (at beginning of job) in TaskService
    long taskTimeStart
    // taskTimeEnd is the time that the completed task was received at TaskService
    long taskTimeEnd
    // executionTimeStart is the time that the task started running on the TaskConsumer
    long executionTimeStart
    // executionTimeEnd is the time that the task ended running on the TaskConsumer
    long executionTimeEnd

    Boolean completed
    Boolean running = false

    // Error info
    String error = ""
    String stdout = ""
    String stderr = ""
    String exitValue = ""

    static belongsTo = [job: Job]

    static constraints = {
    }

    static mapping = {
        stdout type: 'text'
        stderr type: 'text'
        exitValue type: 'text'
    }
}
