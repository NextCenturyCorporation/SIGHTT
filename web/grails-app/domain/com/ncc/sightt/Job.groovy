
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

import com.ncc.sightt.auth.Permissions
import com.ncc.sightt.auth.User
import com.ncc.sightt.JobStatus

class Job {
    String jobName
    User owner
    Permissions permissions

    JobConfig config

    // Job Tasks that get created, one per numImages
    List jobTasks

    // Final zip containing composite images
    String zipFilePath
    Long zipFileSize = 0

    //Keep track of tasks and whether or not we are finished
    Long numComplete
    Long numTasks
    JobStatus status = JobStatus.CREATED

    // Date/time associated with events
    Date submitDate
    Date expectedEndDate
    Date actualEndDate

    // Keep track of the amount of execution time that has been spent on
    // tasks (up to numComplete of them).  Time is in decimal seconds.
    Double sumOfExecutionTime = 0
    Double sumOfExecutionTimeSquared = 0

    Double getMeanOfExecutionTime() {
        if (numComplete > 0) {
            return sumOfExecutionTime / numComplete
        }
        return 30
    }

    Double getVarianceOfExecutionTime() {
        if (numComplete > 0) {
            return (sumOfExecutionTimeSquared / numComplete) - Math.pow(getMeanOfExecutionTime(), 2)
        }
        return 0
    }

    static hasMany = [jobTasks: JobTask]

    static constraints = {

        expectedEndDate(nullable: true)
        actualEndDate(nullable: true)
        zipFilePath(nullable: true)
    }

    static embedded = ['config']

    static transients = [
            'meanOfExecutionTime',
            'varianceOfExecutionTime'
    ]

}
