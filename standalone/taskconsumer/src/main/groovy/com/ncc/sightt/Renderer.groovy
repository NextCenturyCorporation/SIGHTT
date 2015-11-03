
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
import com.ncc.sightt.s3.Utils
import groovy.text.GStringTemplateEngine
import groovy.util.logging.Log4j
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.text.WordUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Given a blender model file, create the appropriate rendered object image.
 */
@Log4j
class Renderer {

    static final String BLENDER_ROTATE_SCRIPT = "resources/blender_rotate_script.py"
    static final String BLENDER_EXPORT_SCRIPT = "resources/blender_export_script.py"
    static final String BASE_OUTPUT_DIRECTORY = "/tmp/render_"
    static final String GENERATED_BLENDER_SCRIPT_FILENAME = "blenderScript.py"

    // Where the work will be done
    File outputDir

    // Whether or not the directory should be deleted on completion.  Set by Spring
    def deleteDirOnCompletion
    
    /**
     * Generate the script, then tell BlenderCaller to produce output, then determine values
     */
    def getRenderedCompositeImageFromBlender(blenderModelPath, backgroundPath,
                                             BaseRenderMessage message) {

        log.debug("model: " + blenderModelPath + " background: " + backgroundPath)

        def outputDirName = createOutputDir(message)
        def generatedRotateScript = generateTemplatedBlenderScriptFile(message,
                backgroundPath, outputDirName, BLENDER_ROTATE_SCRIPT)
        BaseRenderMessage finishedMessage = new BlenderCaller().renderWithBlender(
                blenderModelPath, generatedRotateScript, message)

        if (message.generateObjectFile) {
            String resultOfExport = exportObjectModel(message, backgroundPath,
                    outputDirName, blenderModelPath)
            finishedMessage.error = finishedMessage.error + resultOfExport
        }
        finishedMessage = setMessageValues(finishedMessage, outputDirName)
        return finishedMessage
    }

    /**
     * Create the output directory for this rendering
     */
    def createOutputDir(BaseRenderMessage message,
                        baseOutputDirectory = BASE_OUTPUT_DIRECTORY) {

        def outputDirName = baseOutputDirectory + Utils.getBasenameForModelName(
                message.modelName, message.yaw, message.pitch, message.roll) +
                RandomStringUtils.random(16, true, true) + "/"
        log.debug("Output directory name: " + outputDirName)

        outputDir = new File(outputDirName)
        outputDir.mkdir()
        if (!outputDir.exists()) {
            throw new Exception("Unable to create output directory " + outputDirName)
        }
        return outputDirName
    }

    /** 
     * Remove the output directory 
     */
     def removeOutputDirectory()
     {
         log.info("Value of delete on completion: " + deleteDirOnCompletion)

         if (!deleteDirOnCompletion) {
            return
         }

         if (!outputDir.exists()) {  
            log.info("Output dir does not exist: " + outputDir)
            return
         }

         // Try to remove the directory.  Catch any errors and report, but do not 
         // stop processing. 
         try {
             def result = outputDir.deleteDir()         
             if (!result) {
                 log.info("Unable to delete directory " + outputDir)
             }
         }
         catch (Exception e) {
             log.info("Unable to delete directory " + outputDir)
             e.printStackTrace()
         }
         
     }

    /**
     *  Apply the parameters (outputdir, background, x,y,z rotations) to the blender script file
     */
    def generateTemplatedBlenderScriptFile(BaseRenderMessage message,
                                           String backgroundPath, String outputDirName, String script) {

        def blenderScriptFilename = getBlenderScriptFilename(script)
        def generatedBlenderScriptFilename = outputDirName +
                GENERATED_BLENDER_SCRIPT_FILENAME

        def translation = getModelTranslationValues(backgroundPath, message.pointX, message.pointY)
        log.warn("Translation values: " + translation.x + " " + translation.y)
        log.warn("yaw pitch roll: " + message.yaw + " " + message.pitch + " " + message.roll)

        scriptReplace(blenderScriptFilename, generatedBlenderScriptFilename,
                outputDirName, backgroundPath, translation.x, translation.y, message.yaw,
                message.pitch, message.roll, message.scaleFactor,
                message.generateAllMasks, message.useLightingModel, message.sunLocation, message.sunIntensity,
                message.sunColor, message.useAmbient, message.ambientIntensity, message.ambientSamples,
                message.useGroundPlaneModel, message.groundPositionX, message.groundPositionY, message.groundPositionZ,
                message.groundRotationX, message.groundRotationY)
        if (!new File(generatedBlenderScriptFilename).exists()) {
            log.warn("Unable to create ${generatedBlenderScriptFilename}")
            throw new Exception("No script ${generatedBlenderScriptFilename}")
        }
        return generatedBlenderScriptFilename
    }

    /**
     * Convert x and y values in image coordinates to translation-from-center coordinates
     * since Blender does a translation
     */
    def getModelTranslationValues(backgroundPath, x, y) {

        // Special case: if x/y not set, do not translate
        if (!x || !y) {
            return [x: 0, y: 0]
        }

        BufferedImage bi = ImageIO.read(new File(backgroundPath))
        def width = bi.getWidth()
        def height = bi.getHeight()

        return convertToBlenderTranslation(width, height, x, y)
    }

    def convertToBlenderTranslation(width, height, x, y) {
        def centerX = width / 2
        def translateX = x - centerX
        def centerY = height / 2
        def translateY = -(y - centerY)
        return [x: translateX, y: translateY]
    }

    /**
     * Replace the parameters in the blender rotate script with the passed parameters
     */
    void scriptReplace(String scriptIn, String scriptOut, outputDir,
                       backgroundPath, translateX, translateY, yaw, pitch, roll, scaleFactor,
                       generateAllMasks, Boolean useLightingModel, sunLocation, sunIntensity, sunColor, useAmbient,
                       ambientIntensity, ambientSamples, Boolean useGroundPlaneModel, groundPositionX, groundPositionY,
                       groundPositionZ, groundRotationX, groundRotationY) {

        File fin = new File(scriptIn)

        // The following means to replace the string '${backgroundPath}' with the value of backgroundPath
        def binding = [
                outputDir: outputDir,
                backgroundPath: backgroundPath,
                modelPointX: translateX,
                modelPointY: translateY,
                yaw: yaw,
                pitch: pitch,
                roll: roll,
                scaleFactor: scaleFactor,
                allLayers: WordUtils.capitalize(String.valueOf(generateAllMasks)),
                dirLocation: sunLocation,
                dirIntensity: sunIntensity,
                dirColor: sunColor,
                useAmbient: useAmbient ? "True" : "False",
                ambIntensity: ambientIntensity,
                ambSamples: ambientSamples,
                useLightingModel: useLightingModel ? "True" : "False",
                useGroundPlaneModel: useGroundPlaneModel ? "True" : "False",
                groundPositionX: groundPositionX,
                groundPositionY: groundPositionY,
                groundPositionZ: groundPositionZ,
                groundRotationX: groundRotationX,
                groundRotationY: groundRotationY

        ]

        String text = fin.text
        def engine = new GStringTemplateEngine()
        def writable = engine.createTemplate(text).make(binding)

        File fout = new File(scriptOut)
        fout << writable

        log.debug("Created new script ${scriptOut}")
    }

    /**
     *  Make sure that the blender script exists in the filepath, also checking in src/dist, because
     *  that's where it is in during testing
     */
    def getBlenderScriptFilename(String script) {

        if (new File(script).exists()) {
            return script
        }

        String scriptInSrcDist = "src/dist/" + script
        if (new File(scriptInSrcDist).exists()) {
            log.debug("Using ${scriptInSrcDist} for blender script " + script)
            return scriptInSrcDist
        }
        throw new Exception("Cannot find ${script}")
    }

    /**
     * Calls the blender export script to save the model as an object (.obj) file.
     * Returns the error from the blender export job, if any.
     */
    def exportObjectModel(BaseRenderMessage message, String backgroundPath,
                          String outputDirName, String blenderModelPath) {

        def generatedExportScript = generateTemplatedBlenderScriptFile(message,
                backgroundPath, outputDirName, BLENDER_EXPORT_SCRIPT)
        BaseRenderMessage exportMessage = new BlenderCaller().renderWithBlender(
                blenderModelPath, generatedExportScript, message)
        return exportMessage.error
    }

    /**
     * Use the returned info from running the render to determine what to put into the message
     */
    def setMessageValues(BaseRenderMessage message, String outputDirName) {

        File compositeFile = Utils.findMatchingFile(outputDirName, "composite.png")
        if (compositeFile?.exists()) {
            log.debug("Composite file:  ${compositeFile.absolutePath}")
            message.compositeImageFilename = compositeFile.absolutePath
        } else {
            message.error = message.error + "\nComposite image file does not exist."
            return message
        }

        File maskFile = Utils.findMatchingFile(outputDirName, "compositemask\\.*\\.png")
        if (maskFile?.exists()) {
            log.debug("Mask file:  ${maskFile.absolutePath}")
            message.maskImageFilename = maskFile.absolutePath
        } else {
            message.error = message.error + "\nMask image file does not exist."
            return message
        }

        File objFile = Utils.findMatchingFile(outputDirName, "object.obj")
        if (objFile?.exists()) {
            log.debug("Object (.obj) file:  ${objFile.absolutePath}")
            message.objFilename = objFile.absolutePath
        } else if (message.generateObjectFile) {
            message.error = message.error + "\nObject (.obj) file does not exist."
            return message
        }

        // Determine the masks for parts, if any
        message.partMaskImageFilenames = []
        message.partMaskKeys = []
        if (message.generateAllMasks) {
            List<File> partMaskFiles = Utils.findMatchingFiles(outputDirName, "mask_.*\\.png")
            partMaskFiles?.each {
                message.partMaskImageFilenames.add(it)
            }
        }

        return message
    }
}
