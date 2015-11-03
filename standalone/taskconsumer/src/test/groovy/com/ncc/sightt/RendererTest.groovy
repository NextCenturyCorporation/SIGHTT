
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

import com.ncc.sightt.message.TaskRenderMessage
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RendererTest {

    static final MODEL_FILE = "MODEL"

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    TaskRenderMessage message
    File model
    File script

    @Before
    void setUp() {
        message = new TaskRenderMessage()
        message.modelName = "ABC7"
        message.scaleFactor = 0.5
        message.pointX = 0
        message.pointY = 0
        message.generateAllMasks = false
        message.yaw = 0
        message.pitch = 0.3
        message.roll = -0.1

        model = folder.newFile("model.blend")
        script = folder.newFile("script.py")
    }

    @After
    void tearDown() {
        model.delete()
        script.delete()
    }

    @Test
    void testCreateOutputDir() {
        Renderer r = new Renderer()
        String outputDirName = r.createOutputDir(message,
                folder.getRoot().absolutePath)
        File f = new File(outputDirName)
        assert f.exists()
    }

    @Test
    void testGenerateBlenderScriptFile() {
        Random random = new Random(new Date().getTime())
        message.yaw = random.nextFloat()
        message.pitch = random.nextFloat()
        message.roll = random.nextFloat()
        message.scaleFactor = 1.0
        message.pointX = 0
        message.pointY = 0

        Renderer r = new Renderer()
        String outputDirectoryFilename = r.createOutputDir(message,
                folder.getRoot().absolutePath)
        String script = r.generateTemplatedBlenderScriptFile(message,
                UtilMethodsForTests.BACKGROUND_FILE, outputDirectoryFilename,
                Renderer.BLENDER_ROTATE_SCRIPT)
        File f = new File(script)
        assert f.exists()
    }

    @Test
    void testSetMessageValues() {
        message.compositeImageFilename = ""
        message.maskImageFilename = ""
        message.objFilename = ""
        message.error = ""
        message.partMaskImageFilenames = []
        message.generateAllMasks = false

        def composite = folder.newFile("composite.png")
        def mask = folder.newFile("compositemask.png")
        def obj = folder.newFile("object.obj")

        Renderer renderer = new Renderer()
        TaskRenderMessage finishedMessage = renderer.setMessageValues(message,
                folder.getRoot().absolutePath)

        assert message.compositeImageFilename == composite.absolutePath
        assert message.maskImageFilename == mask.absolutePath
        assert message.objFilename == obj.absolutePath
        assert message.error == ""
        assert message.partMaskImageFilenames.size() == 0

        composite.delete()
        mask.delete()
        obj.delete()
    }

    @Test
    void testSetMessageValuesWithParts() {
        message.compositeImageFilename = ""
        message.maskImageFilename = ""
        message.objFilename = ""
        message.error = ""
        message.partMaskImageFilenames = []
        message.generateAllMasks = true

        def composite = folder.newFile("composite.png")
        def mask = folder.newFile("compositemask.png")
        def obj = folder.newFile("object.obj")
        def part1 = folder.newFile("mask_1.png")
        def part2 = folder.newFile("mask_2.png")

        Renderer renderer = new Renderer()
        TaskRenderMessage finishedMessage = renderer.setMessageValues(message,
                folder.getRoot().absolutePath)

        assert message.compositeImageFilename == composite.absolutePath
        assert message.maskImageFilename == mask.absolutePath
        assert message.objFilename == obj.absolutePath
        assert message.error == ""
        assert message.partMaskImageFilenames.size() == 2
        assert message.partMaskImageFilenames.contains(part1)
        assert message.partMaskImageFilenames.contains(part2)

        composite.delete()
        mask.delete()
        obj.delete()
        part1.delete()
        part2.delete()
    }

    @Test
    void testSetMessageValuesFailOnComposite() {
        message.error = ""

        Renderer renderer = new Renderer()
        TaskRenderMessage finishedMessage = renderer.setMessageValues(message,
                folder.getRoot().absolutePath)

        assert message.error != ""
    }

    @Test
    void testSetMessageValuesFailOnMask() {
        message.compositeImageFilename = ""
        message.error = ""

        def composite = folder.newFile("composite.png")

        Renderer renderer = new Renderer()
        TaskRenderMessage finishedMessage = renderer.setMessageValues(message,
                folder.getRoot().absolutePath)

        assert message.compositeImageFilename == composite.absolutePath
        assert message.error != ""

        composite.delete()
    }

    @Test
    void testSetMessageValuesFailOnObjFile() {
        message.compositeImageFilename = ""
        message.maskImageFilename = ""
        message.error = ""
        message.generateObjectFile = true

        def composite = folder.newFile("composite.png")
        def mask = folder.newFile("compositemask.png")

        Renderer renderer = new Renderer()
        TaskRenderMessage finishedMessage = renderer.setMessageValues(message,
                folder.getRoot().absolutePath)

        assert message.compositeImageFilename == composite.absolutePath
        assert message.maskImageFilename == mask.absolutePath
        assert message.error != ""

        composite.delete()
        mask.delete()
    }

    // Commented because it does not work on Jenkins
    // @Test
    void testRender() {
        message.yaw = 0.002
        message.pitch = -0.12
        message.roll = 0.23
        message.pointX = 123
        message.pointY = 39
        message.scaleFactor = 0.5

        // Copy the blender and background files to a common place
        def modelFile = "/tmp/blender.blend"
        FileUtils.copyFile(new File(UtilMethodsForTests.BLENDER_FILE), new File(modelFile))
        def backgroundFile = "/tmp/background.jpg"
        FileUtils.copyFile(new File(UtilMethodsForTests.BACKGROUND_FILE), new File(backgroundFile))

        Renderer r = new Renderer()
        r.getRenderedCompositeImageFromBlender(modelFile, backgroundFile, message)
        def compFile = new File(message.compositeImageFilename)
        def maskFile = new File(message.maskImageFilename)
        assert compFile?.exists()
        assert maskFile?.exists()
    }

    // Commented because it does not work on Jenkins
    // @Test
    void testRenderModel() {
        message.yaw = 0
        message.pitch = 0
        message.roll = 0
        message.scaleFactor = 1

        // Copy the blender and background files to a common place
        def modelFile = "/tmp/blender.blend"
        FileUtils.copyFile(new File(UtilMethodsForTests.BLENDER_FILE), new File(modelFile))
        def backgroundFile = "/tmp/background.jpg"
        FileUtils.copyFile(new File(UtilMethodsForTests.BACKGROUND_FILE), new File(backgroundFile))

        Renderer r = new Renderer()
        def finishedMessage = r.getRenderedCompositeImageFromBlender(modelFile, backgroundFile, message)
        def compFile = new File(message.compositeImageFilename)
        def maskFile = new File(message.maskImageFilename)
        assert compFile?.exists()
        assert maskFile?.exists()
    }

    // Commented because it does not work on Jenkins
    // @Test
    void testRenderMultiLayer() {
        message.yaw = 0.002
        message.pitch = -0.12
        message.roll = 0.23
        message.scaleFactor = 0.5
        message.pointX = 123
        message.pointY = 39
        message.generateAllMasks = true

        // Copy the blender and background files to a common place
        def modelFile = "/tmp/blender.blend"
        FileUtils.copyFile(new File(UtilMethodsForTests.MULTI_LAYER_BLENDER_FILE), new File(modelFile))
        def backgroundFile = "/tmp/background.jpg"
        FileUtils.copyFile(new File(UtilMethodsForTests.BACKGROUND_FILE), new File(backgroundFile))

        Renderer r = new Renderer()
        r.getRenderedCompositeImageFromBlender(modelFile, backgroundFile, message)
        def compFile = new File(message.compositeImageFilename)
        def maskFile = new File(message.maskImageFilename)
        assert compFile?.exists()
        assert maskFile?.exists()
    }

    // Commented because it does not work on Jenkins
    // @Test
    void testRenderWithLayersTurnedOff() {
        message.yaw = 0.002
        message.pitch = -0.12
        message.roll = 0.23
        message.scaleFactor = 0.5
        message.pointX = 123
        message.pointY = 39

        // Copy the blender and background files to a common place
        def modelFile = "/tmp/blender.blend"
        FileUtils.copyFile(new File(UtilMethodsForTests.MULTI_LAYER_BLENDER_FILE), new File(modelFile))
        def backgroundFile = "/tmp/background.jpg"
        FileUtils.copyFile(new File(UtilMethodsForTests.BACKGROUND_FILE), new File(backgroundFile))

        Renderer r = new Renderer()
        r.getRenderedCompositeImageFromBlender(modelFile, backgroundFile, message)
        def compFile = new File(message.compositeImageFilename)
        def maskFile = new File(message.maskImageFilename)
        assert compFile?.exists()
        assert maskFile?.exists()
    }

    @Test
    void testConvertToBlenderTranslation() {

        Renderer r = new Renderer()

        // The image is 400 x 300.
        //
        // We want the point at 350,125, which means that
        // (from the center), we have to go +x by 150  and +y by 25
        def translate = r.convertToBlenderTranslation(400, 300, 350, 125)
        assert translate.x == 150
        assert translate.y == 25

        translate = r.convertToBlenderTranslation(400, 300, 1, 1)
        assert translate.x == -199
        assert translate.y == 149

        translate = r.convertToBlenderTranslation(400, 300, 399, 299)
        assert translate.x == 199
        assert translate.y == -149

    }
}
