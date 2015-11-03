
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
package com.ncc.sightt.dbupload
import com.ncc.sightt.ModelType
import com.ncc.sightt.ObjectModel
import com.ncc.sightt.RenderedView
import com.ncc.sightt.Thumbnail

import java.awt.image.BufferedImage
/**
 * S3Uploaded receives a S3UploadConfiguration that defines the ObjectModel information and fills
 * out the database corresponding to the configuration, including both defined information and
 * computed information
 */
class S3UploaderService {

    def fileStorageService
    def imageService

    def upload(S3UploadConfiguration s3uploadConfig) {

        // Create the basic object model
        def bucketName = fileStorageService.fileStorageConfiguration.bucketName
        ObjectModel model = new ObjectModel(name: s3uploadConfig.name, modelType: ModelType.BLENDER,
                bucketName: bucketName, filePath: s3uploadConfig.filePath,
                sizeInMeters: s3uploadConfig.sizeInMeters)

        // Get the first rendered view, for height, width, and thumbnail
        String filepathOfFirstRenderedViewName = s3uploadConfig?.renderedViewFilepaths.first()
        BufferedImage bi = imageService.getRawImage(filepathOfFirstRenderedViewName)
        model.renderHeight = bi.height;
        model.renderWidth = bi.width;

        // Generate and save the thumbnail
        def thumb = new Thumbnail()
        if (!thumb.save()) {
            log.warn "Saving thumbnail for model ${model.name} failed:"
            thumb.errors.each { log.warn it }
        }
        imageService.generateAndStoreThumbnailFromStoredImage(thumb.id, filepathOfFirstRenderedViewName)
        model.thumbnail = thumb;

        // Add all the rendered views to the DB
        s3uploadConfig.renderedViewFilepaths.each { e ->
            def name = extractImageName(e.toString())
            def fileKey = e
            def width = model.renderWidth
            def height = model.renderHeight
            def objectSize = model.sizeInMeters
            def renderedView = new RenderedView(sizeInMeters: objectSize, name: name,
                    bucketName: bucketName, filePath: e.toString(), width: width, height: height)
            if (!renderedView.save()) {
                log.warn "Saving rendered view for model ${model.name} failed:"
                renderedView.errors.each { log.warn it }
            }
            model.addToRenderedViews(renderedView)
        }
        if (!model.save()) {
            log.warn "Saving object model ${model.name} failed:"
            model.errors.each { log.warn it }
        }
        model.id
    }

    /**
     * Given a path like /foo/bar/image.png, we want 'image.png' (i.e. strip off all the
     * directory parts.  For 'image3.png', we want 'image3.png'.  For '\bar\baz\abc.jpeg',
     * we want 'abc.jpeg'.
     */
    def extractImageName(String filepath) {
        // See if there are any slashs
        int index = filepath.lastIndexOf("/");
        if (index != -1 && (index + 1) < filepath.length()) {
            return filepath.substring(index + 1);
        }

        // See if there are any backslashes
        index = filepath.lastIndexOf("\\");
        if (index != -1 && (index + 1) < filepath.length()) {
            return filepath.substring(index + 1);
        }

        // Just return the original filepath
        filepath
    }

    def readInConfig(String s) {
        def records = new XmlSlurper().parseText(s)
        def name = records.name
        def filePath = records.filePath
        def sizeInMeters = records.sizeInMeters.toDouble()
        def renderedViewFilepaths = records.renderedViewFilepaths;

        def config = new S3UploadConfiguration();
        config.name = name
        config.filePath = filePath
        config.sizeInMeters = sizeInMeters
        config.renderedViewFilepaths = new ArrayList<String>()
        renderedViewFilepaths.each { path -> config.renderedViewFilepaths.add(path) }

        println("Config: ${config}")
        config
    }

}
