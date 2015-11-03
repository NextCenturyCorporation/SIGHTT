
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

import groovy.util.logging.Log4j

import org.apache.commons.io.FileUtils

/**
 *
 */
@Log4j
class LocalFileStorageService extends FileStorageService {

    File baseDirectory

    LocalFileStorageService() {
    }

    /**
     * Get the base directory that Local File writes and reads from
     */
    def getBaseDir() {
        if (baseDirectory != null && baseDirectory.exists()) {
            return baseDirectory.getPath()
        }
        String baseDirName = fileStorageConfiguration.bucketName
        if (!baseDirName.startsWith("/")) {
            baseDirName = "/tmp/" + baseDirName


        }
        log.debug "BaseDir set as: ${baseDirName}"
        baseDirectory = new File(baseDirName)
        if (baseDirectory.exists()) {
            log.info "Directory ${baseDirName} exists."
        } else {
            boolean b = baseDirectory.mkdir()
            if (!b) {
                log.error("Unable to create directory ${baseDirName}")
            }
        }
        return baseDirName
    }

    boolean checkStorageLocation() {
        return true
    }

    /**
     * Check if a file already exists in the store. filePath does NOT include the bucket name
     */
    Boolean checkIfFileExists(filePath) {
        String fullFilePathName = getBaseDir() + File.separator + filePath
        return (new File(fullFilePathName).exists())
    }

    /**
     * Save a file to the backing store, where prefix is something like "back/img1" or "render/".
     * file is a local disk file, and suffix is "png" or "blend".
     * Returns the file path, like "back/img1_J2JsDPswAqDP23a.png", without bucket name
     */
    String saveFileToStore(String prefix, File file, String suffix = null) {
        String uri = getUniqueFileURI(prefix, suffix)
        String fullFilePathName = getBaseDir() + File.separator + uri
        FileUtils.copyFile(file, new File(fullFilePathName))
        return uri
    }

    /**
     * Load a file from the store into a local file and return the File.
     *
     * The file path should be something like "render/LAG9KRS7G5Z3AN.jpg", a full file key
     */
    File loadFileFromStore(filePath) {
        String fullFilePathName = getBaseDir() + File.separator + filePath
        return (new File(fullFilePathName))
    }

    /**
     * Get a raw object from the data store.
     */
    InputStream retrieveObjectFromStoreAsStream(String filePath) {
        File f = loadFileFromStore(filePath)
        InputStream is = new FileInputStream(f)
        is
    }

    /**
     * Given a file path, return the URI that can be used in an &gt;img&lt; tag to display the image
     */
    String getImageSrcURI(filePath) {
        URL url
        if (fileStorageConfiguration.frontendURL) {
	  url = new URL(fileStorageConfiguration.frontendURL + filePath)
        } else {
            File f = loadFileFromStore(filePath)
            url = f.toURI().toURL()
        }
        return url.toExternalForm()
    }
}
