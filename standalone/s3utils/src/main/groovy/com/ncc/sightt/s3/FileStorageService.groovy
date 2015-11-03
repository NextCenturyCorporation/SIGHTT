
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

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

abstract class FileStorageService {

    def fileStorageConfiguration

    /**
     * Save a file to the backing store, where prefix is something like "back/img1" or "render/".
     * file is a local disk file, and suffix is "png" or "blend".
     * Returns the file path, like "back/img1_J2JsDPswAqDP23a.png", without bucket name
     */
    abstract String saveFileToStore(String prefix, File file, String suffix = null)

    /**
     * Load a file from the store into a local file and return the File.
     *
     * The file path should be something like "render/LAG9KRS7G5Z3AN.jpg", a full file key
     */
    abstract File loadFileFromStore(filePath)

    /**
     * Check if a file already exists in the store. filePath does NOT include the bucket name
     */
    abstract Boolean checkIfFileExists(filePath)

    /**
     * Make the location in the store that will hold the files, trying to create it if not.  Note that
     * in the context of S3, the storage location is the Bucket, keyed from the bucket name
     */
    abstract boolean checkStorageLocation()

    /**
     * Get a raw object from the data store.
     */
    abstract InputStream retrieveObjectFromStoreAsStream(String filePath)

    /**
     * Given a file path, return the URI that can be used in an &gt;img&lt; tag to display the image
     */
    abstract String getImageSrcURI(filePath)

    /**
     * Save a BufferedImage in the file store as a file with the given prefix
     * @param prefix
     * @param bufImg
     * @return the filePath of the stored file
     */
    def saveImageWithPrefix(BufferedImage bufImg, prefix, extension = fileStorageConfiguration.defaultImageType) {
        def bufImgFile = getTempFile()
        bufImgFile.withOutputStream {
            println("Writing buffered image ${bufImgFile.path} with extension ${extension}")
            ImageIO.write(bufImg, extension, it)
        }
        def filePath = saveFileToStore(prefix, bufImgFile, extension)
        deleteTempFile(bufImgFile)
        filePath
    }

    /**
     * Wrapper to saveImageWithPrefix that automatically sets the correct prefix for a Background
     */
    def saveBackgroundImage(bufImg, name = "") {
        saveImageWithPrefix(bufImg, fileStorageConfiguration.backgroundPrefix + name)
    }

    /**
     * Wrapper to saveFileToStore that automatically sets the correct prefix for an Object
     */
    def saveRenderedFile(imgFile, name = "") {
        saveFileToStore(fileStorageConfiguration.renderPrefix + name, imgFile)
    }

    /**
     * Wrapper to saveImageWithPrefix that automatically sets the correct prefix for a Thumbnail
     */
    def saveThumbnailImage(bufImg, name = "") {
        saveImageWithPrefix(bufImg, fileStorageConfiguration.thumbnailPrefix + name)
    }

    /**
     * Wrapper to saveImageWithPrefix that automatically sets the correct prefix for a CompositeImage
     */
    def saveCompositedImage(bufImg, name = "", extension = fileStorageConfiguration.defaultImageType) {
        saveImageWithPrefix(bufImg, fileStorageConfiguration.compositedPrefix + name, extension)
    }

    /**
     * Given a prefix, return a unique path in the file store
     * @param prefix
     * @return the unique file path in the file store
     */
    def getUniqueFileURI(prefix, suffix = null) {
        println("Generating unique URI from base: ${fileStorageConfiguration.bucketName} with prefix ${prefix}")
        if (!suffix) {
            suffix = "png"
        }
        def uniqueFileFound = false
        def uniqueFilePath
        while (!uniqueFileFound) {
            String randomString = RandomStringUtils.random(16, true, true)
            uniqueFilePath = "${prefix}${randomString}.${suffix}"
            uniqueFileFound = !checkIfFileExists(uniqueFilePath)
        }
        uniqueFilePath
    }

    /**
     * Load an image from the file store as a BufferedImage
     * @param fileURI the path in the file store of the image
     * @return a BufferedImage representing the image
     */
    BufferedImage getImage(filePath) {
        File imageFile = loadFileFromStore(filePath)
        BufferedImage originalImage = ImageIO.read(imageFile);
        originalImage
    }

    /**
     * Store an uploaded file to the file store.  this is only used for
     * storing a file that has been uploaded by the web client user.  It has
     * to be stored in a temporary file on the disk before it can be moved
     * to S3.  For most usage, use saveFileToStore directly.
     */
    def storeUploadedFile(prefix, File file, suffix = null) {
        def tempFile = getTempFile(suffix)
        if (file instanceof MultipartFile) {
            println("Saving a MultipartFile")
            file.transferTo(tempFile)
        } else {
            println("Saving a regular file to ${tempFile}")
            tempFile.bytes = file.bytes
        }
        println("Storing uploaded image to: ${tempFile.path}")

        def fileKey = saveFileToStore(prefix, tempFile, suffix)
        deleteTempFile(tempFile)
        println("Image stored to S3 at: ${fileKey}")
        fileKey
    }

    /**
     * Store an uploaded image in the file store and calculate the image height and width
     */
    def bootStrapStoreBackground(file) {
        def bufImg = ImageIO.read(file)
        def fileKey = saveBackgroundImage(bufImg)
        println("Stored background to: ${fileKey}")
        def width = bufImg.width
        def height = bufImg.height
        [filePath: fileKey, width: width, height: height]
    }

    def storeFileLocally(file) {
        if (file instanceof MultipartFile) {
            def tmpFile = getTempFile()
            file.transferTo(tmpFile)
            return tmpFile
        } else {
            return file
        }
    }

    /**
     * Store an uploaded RenderedView in the file store and calculate the image height and width
     * @param file
     * @return
     */
    def storeUploadedRender(file) {
        def localFile = checkForMultipartFile(file)
        def fileKey = saveRenderedFile(localFile)
        println("Stored render to: ${fileKey}")
        def bufImg = ImageIO.read(localFile.file)
        def width = bufImg.width
        def height = bufImg.height
        if (localFile.isTempFile) {
            localFile.file.delete()
        }
        [filePath: fileKey, width: width, height: height]
    }

    /**
     * Store an uploaded 3d model into the store
     * @param file
     * @return
     */
    def storeUploadedModel(file) {
        def localFile = checkForMultipartFile(file)
        def fileKey = storeUploadedFile(fileStorageConfiguration.modelPrefix, localFile.file, localFile.suffix)
        println("Stored model to: ${fileKey}")
        if (localFile.isTempFile) {
            localFile.file.delete()
        }
        fileKey
    }

    def checkForMultipartFile(file) {
        def suffix
        def fileIsTemp
        def localFile
        if (file instanceof CommonsMultipartFile) {
            //This is from a controller
            suffix = Utils.getFilePathSuffix(file.originalFilename)
            localFile = getTempFile("")
            file.transferTo(localFile)
            fileIsTemp = true
        } else {
            localFile = file
            suffix = Utils.getFilePathSuffix(localFile.absolutePath)
            fileIsTemp = false
        }

        [file: localFile, suffix: suffix, fileIsTemp: fileIsTemp]
    }

    /**
     * @return a map of the image properties
     */
    def getImageProperties(filePath) {
        def map = [:]
        def img = getImage(filePath)
        map['width'] = img.width
        map['height'] = img.height
        map['filePath'] = filePath
        map
    }

    /**
     * Return a temporary file
     * @return
     */
    def getTempFile(suffix) {
        if (!suffix) {
            suffix = "tmp"
        }
        File tempFile = File.createTempFile('localtmp', ".${suffix}", null);
        tempFile
    }

    /**
     * Delete a temporary file used by the FileStorageService.  This method MUST check to see that deleting the file would be valid!
     * @param file
     * @return
     */
    def deleteTempFile(file) {
        if (file.exists()) {
            file.delete()
        }
    }
}
