
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

import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListBucketsRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream

class S3StorageService extends FileStorageService {

    AWSCredentials myCredentials
    AmazonS3Client amazonS3Client

    boolean bucketLocationExists = false

    S3StorageService() {

        // ------------------------------------------------------------ 
        // PUT CREDENTIAL HERE
        // TODO:  Put them in a config file
        // ------------------------------------------------------------ 
        myCredentials = new BasicAWSCredentials("", "")
        amazonS3Client = new AmazonS3Client(myCredentials);
    }

    @Override
    def uploadFileToS3(String fileKey, File file) {
        checkStorageLocation()
        def res = amazonS3Client.putObject(fileStorageConfiguration.bucketName, fileKey, file)
        res
    }

    @Override
    String saveFileToStore(String prefix, File bufImgFile, String suffix = null) {
        checkStorageLocation()
        def fileKey = getUniqueFileURI(prefix, suffix)
        println("Save file to store with suffix ${suffix}")
        println("Uploading file ${bufImgFile.path} to S3 with fileKey: ${fileKey} " +
                "in bucket ${fileStorageConfiguration.bucketName}")
        if (!checkIfFileExists(fileKey)) {
            try {
                uploadFileToS3(fileKey, bufImgFile)
            } catch (Exception e) {
                println("Unable to put object ${bufImgFile.path} to S3 with fileKey: ${fileKey} ")
            }
        } else {
            println("fileKey: ${fileKey} already existed... unable to save")
            fileKey = null
        }
        fileKey
    }

    /**
     * Used by controllers to get URI to show the image to user in browser
     */
    @Override
    String getImageSrcURI(filePath) {
        checkStorageLocation()
        def c = Calendar.getInstance()
        c.add(Calendar.MINUTE, 5)
        def validDate = c.getTime()
        def thumbUrl = amazonS3Client.generatePresignedUrl(fileStorageConfiguration.bucketName, filePath, validDate)
        thumbUrl.toExternalForm()
    }

    @Override
    File loadFileFromStore(filePath) {
        checkStorageLocation()
        if (checkIfFileExists(filePath)) {
            def suffix = Utils.getFilePathSuffix(filePath)
            def tmpFile = getTempFile(suffix)
            println("Loading image from store into ${tmpFile.path}")
            amazonS3Client.getObject(new GetObjectRequest(fileStorageConfiguration.bucketName, filePath), tmpFile)
            tmpFile
        } else {
            println("File ${filePath} does not exist on S3!")
            return null
        }
    }

    @Override
    InputStream retrieveObjectFromStoreAsStream(String filePath) {
        S3Object object = amazonS3Client.getObject(fileStorageConfiguration.bucketName, filePath)
        S3ObjectInputStream stream = object.getObjectContent()
    }

    @Override
    Boolean checkIfFileExists(fileKey) {
        checkStorageLocation()
        try {
            def metaData = amazonS3Client.getObjectMetadata(fileStorageConfiguration.bucketName, fileKey)
            true
        } catch (e) {
            false
        }
    }

    @Override
    boolean checkStorageLocation() {
        // Only check once
        if (bucketLocationExists) {
            return true
        }
        boolean exists = doesBucketExist()
        if (!exists) {
            println("Creating the bucket: ${fileStorageConfiguration.bucketName}")
            amazonS3Client.createBucket(fileStorageConfiguration.bucketName)
            exists = doesBucketExist()
            if (!exists) {
                println("Bucket does not exist and cannot create ${FileStorageConfiguration.bucketName}")
                return false
            }
        }
        bucketLocationExists = true
    }

    /**
     * Check for the existence of the bucket. Code here is from https://forums.aws.amazon.com/thread.jspa?threadID=45263
     */
    boolean doesBucketExist() {
        try {
            /* See if the bucket already exists
             * If a bucket DOESN'T exist at all, trying to list its objects
             * returns a 404 NoSuchBucket error response from Amazon S3.
             *
             * Notice that we supply the bucket name in the request and specify
             * that we want 0 keys returned since we don't actually care about the data.
             */
            amazonS3Client.listObjects(new ListObjectsRequest(fileStorageConfiguration.bucketName, null, null, null, 0));
        } catch (AmazonServiceException ase) {
            return false
        }
        return true
    }

}
