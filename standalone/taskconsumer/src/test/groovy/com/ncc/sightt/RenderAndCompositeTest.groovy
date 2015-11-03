
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
package com.ncc.sightt;

import java.awt.Point
import java.awt.image.BufferedImage
import java.util.Map;

import javax.imageio.ImageIO
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.ncc.sightt.message.TaskRenderMessage
import com.ncc.sightt.metadata.Annotation
import com.ncc.sightt.s3.FileStorageService
import com.ncc.sightt.s3.S3StorageService

class RenderAndCompositeTest {

    def static final String bucketName = "sightt-test"
    def static final OBJECT_FILENAME = "model.blend"
    def static final BACKGROUND_FILENAME = "background.png"
    def mockFileStorageService
    File backgroundFile
    File modelFile
    TaskRenderMessage message

    static String objectFile = "resources/image_0_0_0.png"

    @Before
    void setUp() {

        message = new TaskRenderMessage()
        message.scaleFactor = 0.5
        message.pointX = 0
        message.pointY = 0
        message.yaw = 0
        message.pitch = 0.3
        message.roll = -0.1

        backgroundFile = File.createTempFile("background", ".ract.tmp")
        modelFile = File.createTempFile("model", ".ract.tmp")
        /*
         * when OBJECT_FILENAME or BACKGROUND_FILENAME are requested, we mock out the file storage service
         * and return a file handle to a temporary file
         */
        mockFileStorageService = [
                loadFileFromStore: { path ->
                    def file
                    log.info("Loading file '${path}' from store (mocked)")
                    if (path == OBJECT_FILENAME) {
                        file = modelFile
                    }
                    if (path == BACKGROUND_FILENAME) {
                        file = backgroundFile
                    }
                    file
                }

        ] as FileStorageService
    }

    @After
    void tearDown() {
        if (backgroundFile.exists()) {
            backgroundFile.delete()
        }
        if (modelFile.exists()) {
            modelFile.delete()
        }
    }

    @Test
    void testCreateMetadata() {

        // Set up the message inputs
        TaskRenderMessage message = new TaskRenderMessage()
        message.bucketName = "s3bucket/back/"
        message.backgroundName = "Joes Backyard"
        String backFileName = "background.jpg"
        message.backgroundKey = "s3bucket/back/" + backFileName
        message.modelName = "Big Fork"
        String modelImageFileName = "fork.png"
        message.modelKey = "bucket2/object/render/213/" + modelImageFileName
        message.scaleFactor = 1.0
        message.pointX = 0
        message.pointY = 0
        message.yaw = Math.PI / 180
        message.pitch = -0.5
        message.roll = 0.001

        // Results from the TaskConsumer
        String fileKeyFileName = "composite.bmp"
        message.compositeImageFilename = UtilMethodsForTests.BACKGROUND_FILE
        message.compositeKey = "/tmp/blah/" + fileKeyFileName
        BufferedImage compositeImage = ImageIO.read(new File(message.compositeImageFilename))

        RenderAndComposite rc = new RenderAndComposite()
        def maskBoundingBox = new MutablePair<Point, Point>();
        maskBoundingBox.setLeft(new Point(234, 823))
        maskBoundingBox.setRight(new Point(556, 1023))
        def maskPartBoundingBoxes = [:]

        // The metadata rotations are in degrees; internally they are rad
        def yawTest = (message.yaw * 180 / Math.PI).round()
        def pitchTest = (message.pitch * 180 / Math.PI).round()
        def rollTest = (message.roll * 180 / Math.PI).round()

        message.metadata = rc.generateMetadata(message, compositeImage,
                maskBoundingBox, maskPartBoundingBoxes)

        println message.metadata
        File outputFile = new File("/tmp/metadata.xml")
        outputFile.text = message.metadata

        FileInputStream ss = new FileInputStream(new File("/tmp/metadata.xml"))

        // get the annotation back out
        JAXBContext jaxbContext = JAXBContext.newInstance(Annotation.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Annotation annotation = (Annotation) jaxbUnmarshaller.unmarshal(ss);
        assert fileKeyFileName == annotation.getFilename()
        assert message.backgroundName == annotation.getSource().getDatabase()
        assert backFileName == annotation.getSource().getImage()
        assert (int) compositeImage.getWidth() == Integer.parseInt(annotation.getSize().getWidth())
        assert (int) compositeImage.getHeight() == Integer.parseInt(annotation.getSize().getHeight())
        assert maskBoundingBox.getLeft().x == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(0).getX())
        assert maskBoundingBox.getLeft().y == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(0).getY())
        assert maskBoundingBox.getRight().x == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(1).getX())
        assert maskBoundingBox.getRight().y == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(1).getY())
        assert yawTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getPan())
        assert pitchTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getTilt())
        assert rollTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getRoll())
    }

    @Test
    void testCreateMetadataWithParts() {

        // Set up the message inputs
        TaskRenderMessage message = new TaskRenderMessage()
        message.bucketName = "s3bucket/back/"
        message.backgroundName = "Joes Backyard"
        String backFileName = "background.jpg"
        message.backgroundKey = "s3bucket/back/" + backFileName
        message.modelName = "Big Fork"
        String modelImageFileName = "fork.png"
        message.modelKey = "bucket2/object/render/213/" + modelImageFileName
        message.scaleFactor = 1.0
        message.pointX = 0
        message.pointY = 0
        message.yaw = Math.PI / 180
        message.pitch = -0.5
        message.roll = 0.001
        message.partMaskKeys = [
                "partkey1.png",
                "partkey2.png"
        ]

        // Results from the TaskConsumer
        String fileKeyFileName = "composite.bmp"
        message.compositeImageFilename = UtilMethodsForTests.BACKGROUND_FILE
        message.compositeKey = "/tmp/blah/" + fileKeyFileName
        BufferedImage compositeImage = ImageIO.read(new File(message.compositeImageFilename))

        RenderAndComposite rc = new RenderAndComposite()
        def maskBoundingBox = new MutablePair<Point, Point>();
        maskBoundingBox.setLeft(new Point(234, 823))
        maskBoundingBox.setRight(new Point(556, 1023))

        def maskPartNames = ["part1", "part2"]
        def maskPartBoundingBoxes = [:]
        def bb1 = new MutablePair<Point, Point>();
        bb1.setLeft(new Point(234, 823))
        bb1.setRight(new Point(556, 1023))
        def bb2 = new MutablePair<Point, Point>();
        bb2.setLeft(new Point(234, 823))
        bb2.setRight(new Point(556, 1023))
        maskPartBoundingBoxes.put("part1", bb1)
        maskPartBoundingBoxes.put("part2", bb2)

        // The metadata rotations are in degrees; internally they are rad
        def yawTest = (message.yaw * 180 / Math.PI).round()
        def pitchTest = (message.pitch * 180 / Math.PI).round()
        def rollTest = (message.roll * 180 / Math.PI).round()

        message.metadata = rc.generateMetadata(message, compositeImage,
                maskBoundingBox, maskPartBoundingBoxes)

        println message.metadata
        File outputFile = new File("/tmp/metadata.xml")
        outputFile.text = message.metadata

        FileInputStream ss = new FileInputStream(new File("/tmp/metadata.xml"))

        // get the annotation back out
        JAXBContext jaxbContext = JAXBContext.newInstance(Annotation.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Annotation annotation = (Annotation) jaxbUnmarshaller.unmarshal(ss);
        assert fileKeyFileName == annotation.getFilename()
        assert message.backgroundName == annotation.getSource().getDatabase()
        assert backFileName == annotation.getSource().getImage()
        assert (int) compositeImage.getWidth() == Integer.parseInt(annotation.getSize().getWidth())
        assert (int) compositeImage.getHeight() == Integer.parseInt(annotation.getSize().getHeight())
        assert maskBoundingBox.getLeft().x == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(0).getX())
        assert maskBoundingBox.getLeft().y == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(0).getY())
        assert maskBoundingBox.getRight().x == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(1).getX())
        assert maskBoundingBox.getRight().y == Integer.parseInt(annotation.getObject().get(0).getPolygon().getPt().get(1).getY())
        assert yawTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getPan())
        assert pitchTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getTilt())
        assert rollTest == Integer.parseInt(annotation.getObject().get(0).getViewAngle().getRoll())
    }
}
