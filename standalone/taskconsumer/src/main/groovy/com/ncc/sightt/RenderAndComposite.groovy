
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

import com.ncc.sightt.message.BaseRenderMessage
import com.ncc.sightt.message.ImageType
import com.ncc.sightt.message.ModelRenderMessage
import com.ncc.sightt.metadata.*
import com.ncc.sightt.s3.FileStorageService
import com.ncc.sightt.s3.ModelBackgroundCache
import com.ncc.sightt.s3.Utils
import groovy.util.logging.Log4j
import org.apache.commons.lang3.tuple.Pair

import javax.imageio.ImageIO
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import java.awt.*
import java.awt.image.BufferedImage

import static com.google.common.base.Preconditions.checkArgument

/**
 * Create the Rendered composite image and upload to S3
 */
@Log4j
class RenderAndComposite {

    static final DEFAULT_800x600_TRANS_BACKGROUND_PATH = "resources/800x600_transparent_for_model_view.png"
    static final THUMB_WIDTH = 75

    // Network storage, cache, renderer -- These are injected by a message processor
    FileStorageService storageService
    ModelBackgroundCache cache
    Renderer renderer

    /**
     * For general use, given a model, rotate and translate it, then composite into the background, create
     * the metadata and return all the info to the caller
     */
    def generateAndUploadSceneWithBlender(BaseRenderMessage message) {
        BaseRenderMessage finishedMessage = generateSceneWithBlender(message)

        def imageSize = handleCompositeImage(finishedMessage)
        def maskBoundingBox = handleMaskImage(finishedMessage)
        def maskPartBoundingBoxes = handleMaskImageParts(finishedMessage)

        finishedMessage.metadata = generateMetadata(finishedMessage, imageSize, maskBoundingBox,
                maskPartBoundingBoxes)

        handleObjectFile(finishedMessage)

        renderer.removeOutputDirectory()

        return finishedMessage
    }

    /**
     * Call the renderer and render the image
     */
    def generateSceneWithBlender(BaseRenderMessage message) {
        File backgroundFile = cache.getBackground(message.backgroundKey).result
        File blenderFile = cache.getModel(message.modelKey).result

        checkArgument(backgroundFile?.exists(), "Cannot find background file %s",
                message.backgroundKey)
        checkArgument(blenderFile?.exists(), "Cannot find model file %s",
                message.modelKey)

        BaseRenderMessage finishedMessage =
            renderer.getRenderedCompositeImageFromBlender(blenderFile.absolutePath,
                    backgroundFile.absolutePath, message)

        return finishedMessage
    }

    def handleCompositeImage(BaseRenderMessage message) {
        File outputImage = new File(message.compositeImageFilename)
        if (outputImage?.exists()) {
            def image = ImageIO.read(outputImage)
            message.compositeKey = saveCompositeImageToS3(image,
                    message)
            message.compositeThumbKey = saveThumbnailToS3(image)
            return [width: image.width, height: image.height]
        }
        log.warn("Cannot find output image: " + message.compositeImageFilename)
        return [width: 0, height: 0]
    }

    def saveCompositeImageToS3(BufferedImage compositeImage,
                               BaseRenderMessage message) {
        return saveImage(compositeImage, message)
    }

    def saveImage(BufferedImage image, BaseRenderMessage message,
                  String suffix = "") {

        def baseFilename = Utils.getBasenameForModelName(message.modelName,
                message.yaw, message.pitch, message.roll)

        if (message.imageType == ImageType.JPG) {
            image = Utils.convertPNGToJPG(image)
            return storageService.saveCompositedImage(image, baseFilename + suffix,
                    "jpg")
        }

        return storageService.saveCompositedImage(image, baseFilename + suffix)
    }

    def saveThumbnailToS3(BufferedImage compositeImage) {
        def thumbScaleFactor = THUMB_WIDTH / compositeImage.width
        def thumbHeight = (int) (compositeImage.height * thumbScaleFactor)
        BufferedImage thumbnail = Utils.getImageScaled(compositeImage, THUMB_WIDTH, thumbHeight)
        return storageService.saveThumbnailImage(thumbnail)
    }


    def handleMaskImage(BaseRenderMessage message) {
        def maskFile = new File(message.maskImageFilename)
        if (maskFile?.exists()) {
            def maskImage = ImageIO.read(maskFile)
            def maskBoundingBox = Utils.getBoundingBoxOfNonZeroPixels(maskImage)
            message.maskKey = saveMaskImageToS3(maskImage, message)
            return maskBoundingBox
        }
        log.warn("Cannot find mask image: " + message.maskImageFilename)
        return Utils.getEmptyBoundingBox()
    }

    def saveMaskImageToS3(BufferedImage maskImage,
                          BaseRenderMessage message) {
        return saveImage(maskImage, message, "_mask_")
    }

    def handleMaskImageParts(BaseRenderMessage message) {
        Map<String, Pair<Point, Point>> maskPartBoundingBoxes = [:]
        message.partMaskImageFilenames.each {
            String name = Utils.getFilenameFromFullFilepath(it.absolutePath)
            String modelName = Utils.getModelName(name)
            def partMaskImage = ImageIO.read(it)
            message.partMaskKeys.add(saveImage(partMaskImage, message,
                    "partmask_" + modelName + "_"))
            maskPartBoundingBoxes.put(modelName,
                    Utils.getBoundingBoxOfNonZeroPixels(partMaskImage))
        }
        return maskPartBoundingBoxes
    }

    def handleObjectFile(BaseRenderMessage message) {
        if (!message.generateObjectFile || !message.objFilename) {
            message.objFileKey = ""
            return
        }

        def objFile = new File(message.objFilename)
        if (objFile?.exists()) {
            def objKey = storageService.saveFileToStore(
                    storageService.fileStorageConfiguration.objFilePrefix, objFile,
                    "obj")
            message.objFileKey = objKey
        }
        else
        {
            log.warn("Cannot find object file: " + message.objFilename)
        }
    }

/**
 * For uploading models, create a model view with transparent background and no rotations
 */
    def generateAndUploadModelView(ModelRenderMessage message) {

        // Get background, getting transparent if value not set
        File backgroundFile = getBackgroundFile(message)
        checkArgument(backgroundFile?.exists(), "Background does not exist %s",
                backgroundFile?.absolutePath)

        File blenderFile = cache.getModel(message.modelKey).result
        checkArgument(blenderFile?.exists(), "Model %s does not exist at %s", message.modelKey, blenderFile.path)

        BaseRenderMessage finishedMessage =
            renderer.getRenderedCompositeImageFromBlender(blenderFile.absolutePath,
                    backgroundFile.absolutePath, message)

        def outputFile = new File(finishedMessage.compositeImageFilename)
        checkArgument(outputFile?.exists(), "Composite image does not exist %s",
                finishedMessage.compositeImageFilename)
        finishedMessage.compositeKey = storageService.saveRenderedFile(outputFile)

        finishedMessage.objFileKey = handleObjectFile(finishedMessage)

        return finishedMessage
    }

    def getBackgroundFile(BaseRenderMessage message) {
        if (message.backgroundKey) {
            File backgroundFile = cache.getBackground(message.backgroundKey).result
            if (backgroundFile?.exists()) {
                return backgroundFile
            }
            log.warn("Background key ${message.backgroundKey} does not exist.")
        }

        File backgroundFile = new File(DEFAULT_800x600_TRANS_BACKGROUND_PATH)
        if (backgroundFile?.exists()) {
            return backgroundFile
        }

        backgroundFile = new File("src/dist/" + DEFAULT_800x600_TRANS_BACKGROUND_PATH)
        if (backgroundFile?.exists()) {
            return backgroundFile
        }

        return null
    }

/**
 * Generate metadata object
 */
    def generateMetadata(BaseRenderMessage message,
                         imageSize, Pair<Point, Point> maskBox,
                         Map<String, Pair<Point, Point>> maskPartBoxes) {

        def maskPartNames = maskPartBoxes.keySet()
        Annotation a = new Annotation()
        a.setFilename(Utils.getFilenameFromFullFilepath(message.compositeKey))
        setSource(a, message)
        setSize(a, imageSize)
        setObject(a, message, maskBox, maskPartNames)
        setPartObjects(a, maskPartNames, maskPartBoxes)

        // save the metadata into a string
        JAXBContext jaxbContext = JAXBContext.newInstance(Annotation.class)
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        StringWriter stringWriter = new StringWriter()
        jaxbMarshaller.marshal(a, stringWriter)
        return stringWriter.toString()
    }

    def setObject(Annotation annotation, BaseRenderMessage message,
                  Pair<Point, Point> maskBox, Set<String> maskPartNames) {

        com.ncc.sightt.metadata.Object metadataObject = new com.ncc.sightt.metadata.Object()
        metadataObject.setName(message.modelName)
        metadataObject.setModelName(Utils.getFilenameFromFullFilepath(message.modelKey))
        ViewAngle viewAngle = new ViewAngle()
        viewAngle.setPan(Utils.getClosestIntDegreesForRadians(message.yaw))
        viewAngle.setTilt(Utils.getClosestIntDegreesForRadians(message.pitch))
        viewAngle.setRoll(Utils.getClosestIntDegreesForRadians(message.roll))
        metadataObject.setViewAngle(viewAngle)
        DistanceFromCamera distanceFromCamera = new DistanceFromCamera()
        distanceFromCamera.setDistance("10")
        distanceFromCamera.setUnit("meter")
        metadataObject.setDistanceFromCamera(distanceFromCamera)
        ObjectID objectID = new ObjectID()
        objectID.setvalue(Utils.getFilenameFromFullFilepath(message.modelKey))
        metadataObject.setObjectID(objectID)
        metadataObject.setClassID("1")
        metadataObject.setOcclusion("0")
        Polygon poly = getPoly(maskBox)
        metadataObject.setPolygon(poly)

        // Add the parts of the object
        if (maskPartNames?.size() > 0) {
            ObjectParts parts = new ObjectParts()
            maskPartNames.each {
//This is never used        com.ncc.sightt.metadata.ObjectParts ob = new com.ncc.sightt.metadata.ObjectParts()
                ObjectID partId = new ObjectID()
                partId.setvalue(it)
                parts.getObjectID().add(partId)
            }
            metadataObject.setObjectParts(parts)
        }

        annotation.getObject().add(metadataObject)
    }

    def setPartObjects(Annotation annotation, Set<String> maskPartNames,
                       Map<String, Pair<Point, Point>> maskPartBoxes) {

        maskPartNames.each { part ->
            com.ncc.sightt.metadata.Object metadataObject = new com.ncc.sightt.metadata.Object()
            metadataObject.setName(part)
            ObjectID partId = new ObjectID()
            partId.setvalue(part)
            metadataObject.setObjectID(partId)
            metadataObject.setOcclusion("0")
            metadataObject.setClassID("2")
            Polygon poly = getPoly(maskPartBoxes.get(part))
            metadataObject.setPolygon(poly)
            annotation.getObject().add(metadataObject)
        }
    }

    def Polygon getPoly(Pair<Point, Point> boundingBox) {
        Polygon poly = new Polygon()
        Pt ptTopLeft = new Pt()
        ptTopLeft.setX("" + (int) (boundingBox.getLeft().getX()))
        ptTopLeft.setY("" + (int) (boundingBox.getLeft().getY()))
        poly.getPt().add(ptTopLeft)
        Pt ptBottomRight = new Pt()
        ptBottomRight.setX("" + (int) (boundingBox.getRight().getX()))
        ptBottomRight.setY("" + (int) (boundingBox.getRight().getY()))
        poly.getPt().add(ptBottomRight)
        poly
    }

    def setSize(Annotation annotation, imageSize) {
        Size size = new Size()
        size.setWidth("" + imageSize.width)
        size.setHeight("" + imageSize.height)
        size.setDepth("3")
        annotation.setSize(size)
    }

    def setSource(Annotation annotation, BaseRenderMessage message) {
        Source source = new Source()
        source.setDatabase(message.backgroundName)
        source.setImage(Utils.getFilenameFromFullFilepath(message.backgroundKey))
        annotation.setSource(source)
    }

}
