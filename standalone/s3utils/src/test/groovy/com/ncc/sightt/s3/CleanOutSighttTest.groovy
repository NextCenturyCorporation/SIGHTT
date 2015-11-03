
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
package com.ncc.sightt.s3

import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary

/**
 * Created with IntelliJ IDEA.
 * User: cdorman
 * Date: 11/18/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
class CleanOutSighttTest {

    def bucketName = "sightt-test"

    public static void main(String[] args) {
        CleanOutSighttTest c = new CleanOutSighttTest()
        c.run()
    }

    def run() {
        getInformation()
        boolean clean = promptUser()
        if (clean) {
            cleanOutInformation()
        }
    }

    def getInformation() {
        S3StorageService ss = new S3StorageService()
        FileStorageConfiguration fsc = new FileStorageConfiguration()
        fsc.bucketName = bucketName
        ss.fileStorageConfiguration = fsc

        boolean bucketExists = ss.doesBucketExist()
        println("Bucket: ${fsc.bucketName}.  Does it exist: ${bucketExists}")

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).with
        ObjectListing objectListing
        boolean first = true
        while (first || objectListing.isTruncated()) {
            first = false
            objectListing = ss.amazonS3Client.listObjects(listObjectsRequest)
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                println("Object: " + objectSummary)
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        }
    }


    def promptUser() {
        return false
    }

    def cleanOutInformation() {

    }
}
