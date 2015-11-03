
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

import java.awt.image.BufferedImage

class ImageService {
    def fileStorageService

    static transactional = false
    static final THUMBNAIL_SIZE = 75
    static final IMAGE_STORE = "FILESYSTEM"
    static final DEFAULT_IMAGE_TYPE = "png"

    /**
     * Generate a thumbnail of an image and store it returns the path to the image
     */
    def generateAndStoreThumbnailFromBufferedImage(thumbnailId, BufferedImage image) {
        def thumbnailInstance = Thumbnail.get(thumbnailId)
        def thumbnailBufImg = getImageScaled(image, THUMBNAIL_SIZE, -1)

        def thumbnailPath = fileStorageService.saveThumbnailImage(thumbnailBufImg)
        thumbnailInstance.filePath = thumbnailPath
        thumbnailInstance.valid = true
        if (!thumbnailInstance.save(flush: true)) {
            log.warn "Saving thumbnail ${thumbnailInstance.id} failed:"
            thumbnailInstance.errors.each { log.warn it }
        }
        thumbnailId
    }

    /**
     * return a URL for the thumbnail given by a thumbnail id
     * @param id
     * @return the URL of the thumbnail
     */
    def getThumbnailImageSrc(Long id) {
        def thumb = Thumbnail.get(id)
        fileStorageService.getImageSrcURI(thumb.filePath)
    }

    /**
     * return a list of thumbnail src links (for use in img tags) for a list of images.
     * @param imageList
     * @return the list of src entries
     */
    def getThumbnailSrcList(imageList) {
        def thumbSrcList = [:]
        imageList.each { img ->
            def imgId = img.id
            def imgSrcString = getThumbnailImageSrc(img.thumbnail.id)
            thumbSrcList[imgId] = imgSrcString
        }
        thumbSrcList
    }

    /**
     * Generate a thumbnail of a stored image and store it; returns the path to the image
     */
    def generateAndStoreThumbnailFromStoredImage(thumbnailId, imagePath) {
        def thumbnailInstance = Thumbnail.get(thumbnailId)
        def thumbnailBufImg = getImageScaled(imagePath, THUMBNAIL_SIZE)

        def thumbnailPath = fileStorageService.saveThumbnailImage(thumbnailBufImg)
        thumbnailInstance.filePath = thumbnailPath
        thumbnailInstance.valid = true
        if (!thumbnailInstance.save(flush: true)) {
            log.warn "Saving thumbnail ${thumbnailInstance.id} failed:"
            thumbnailInstance.errors.each { log.warn it }
        }
        thumbnailId
    }

    /**
     * Scale a stored file so that the width (X) is equal to the passed size. Y is scaled
     * to whatever will make X fit
     */
    def getImageScaled(String filePath, int newSize) {
        BufferedImage originalImage = fileStorageService.getImage(filePath)
        BufferedImage resizedImage = getImageScaled(originalImage, newSize, -1)
        resizedImage
    }

    def getImageScaled(BufferedImage originalImage, int xSize, int ySize) {
        java.awt.Image scaledInstance = originalImage.getScaledInstance(xSize, ySize, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(scaledInstance.getWidth(null),
                scaledInstance.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        resizedImage.getGraphics().drawImage(scaledInstance, 0, 0, null)
        resizedImage
    }
}
