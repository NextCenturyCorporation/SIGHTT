
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
package com.ncc.sightt.jobzipper

import static org.junit.Assert.*
import groovy.util.logging.Log4j

import java.util.zip.ZipException

import javax.jms.TextMessage

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.JobMessage
import com.ncc.sightt.message.MetadataMessage
import com.ncc.sightt.message.TaskRenderMessage
import com.ncc.sightt.s3.FileStorageConfiguration
import com.ncc.sightt.s3.S3StorageService
import com.ncc.sightt.s3.FileStorageService


@Log4j
class TestZipper {

    def jz
    static Long DEFAULT_JOB_ID = 984512039L
    static String DEFAULT_JOB_NAME = "laksd34klasdf"
    static JobMessage jobMessage
    static String DEFAULT_BUCKET = "sightt-test"
    final shouldFail = new GroovyTestCase().&shouldFail

    @BeforeClass
    static void expandoMagic() {
        ExpandoMetaClass.enableGlobally()
    }

    @Before
    void setUp() {
        log.info "------ Setup -------"

        jobMessage = new JobMessage()
        jobMessage.jobId = DEFAULT_JOB_ID
        jobMessage.jobName = DEFAULT_JOB_NAME

        def jmsUrl = "vm://localhost"
        jz = new JobZipper()
        jz.jmsUrl = jmsUrl
        def fsc = new FileStorageConfiguration()
        fsc.bucketName = "sightt-test"
        fsc.defaultImageType = "png"
        fsc.backgroundPrefix = "back/"
        fsc.modelPrefix = "model/"
        fsc.thumbnailPrefix = "thumb/"
        fsc.compositedPrefix = "comp/"
        fsc.renderPrefix = "render/"
        jz.initializeAndListen()
    }

    @Test
    void testGetMaskFilenameFromFullFilepath() {
        String fullFilepath = "/s3/back/render_34_42_12_LDSFAJQETJADSV.png"
        String maskName = jz.getMaskFilenameFromFullFilepath(fullFilepath)
        log.debug("Resulting filename for mask: " + maskName)
        assert maskName == "render_34_42_12_LDSFAJQETJADSV.mask.png"
    }

    @Test
    void testGetPartMaskFilenameFromFullFilepath() {
        String fullFilepath = "/s3/back/render_34_42_12_LDSFAJQETJADSV.png"
        String partFilename = "/s3/mask/mask_PART2354.png"
        String maskName = jz.getPartMaskFilenameFromFullFilepath(fullFilepath, partFilename)
        log.debug("Resulting filename for part mask: " + maskName)
    }


    void mockWatchdogTimer() {
        log.info "Mocking watchdog timer to have no delay."
        JobZipper.metaClass.resetWatchdogTimer = { currentJobZip, jobMessage ->
            log.info "Watchdog timer for ${currentJobZip}"
            jz.finishZipUploadAndReport(currentJobZip)
        }
    }

    void unmockWatchdogTimer() {
        log.info "Unmocking watchdog timer"
        GroovySystem.metaClassRegistry.removeMetaClass(JobZipper.class)
    }

    void mockS3StorageService() {
        log.info "Mocking S3StorageService"
        FileStorageService.metaClass.retrieveObjectFromStoreAsStream = { String filePath ->
            log.info "Grabbing ${filePath} "
            InputStream bytes = new ByteArrayInputStream("1".getBytes())
            bytes
        }

        FileStorageService.metaClass.saveFileToStore = { String prefix, File file, String suffix ->
            log.info("Uploading file to store")
            "randomFileKey"
        }
    }

    void unmockS3StorageService() {
        GroovySystem.metaClassRegistry.removeMetaClass(FileStorageService.class)
    }

    def getMockJms() {
        def jms = new Expando()
        jms.createSubscription = { s, dest ->
            log.info "Creating new subscription for ${dest} on ${s}"
            fakeConsumer
        }
        jms.createConsumer = { s, dest ->
            log.info "Creating new consumer for ${dest} on ${s}"
            fakeConsumer
        }
        jms.createProducer = { s, dest -> log.info "Creating new producer for ${dest} on ${s}" }
        jms
    }

    def createValidJobMessage() {
        def jdmsg = [bucketName: DEFAULT_BUCKET, jobId: DEFAULT_JOB_ID, numTasks: 1, zipFilePath: ""] as JobMessage
        def msg = [getText: {-> return JmsUtil.toXML(jdmsg) }] as TextMessage
        msg
    }

    def createValidTaskRenderMessage() {
        def taskMsg = [bucketName: DEFAULT_BUCKET, compositeKey: "foo.bar", jobId: DEFAULT_JOB_ID] as TaskRenderMessage
        def msg = [getText: {-> return JmsUtil.toXML(taskMsg) }] as TextMessage
        msg
    }

    def createValidMetadataMessage() {
        def metaMessage = [jobId: DEFAULT_JOB_ID, compositeFilename: "composite.png", metadata: "<x>blah</x"] as MetadataMessage
        def msg = [getText: {-> return JmsUtil.toXML(metaMessage) }] as TextMessage
        msg
    }

    @Test
    void testGetNewZip() {
        log.info "------ testGetNewZip -------"
        assert (jz.zipfileMap.isEmpty())
        def cjz = jz.getCurrentJobZip(jobMessage)
        assert (jz.zipfileMap[DEFAULT_JOB_ID] != null)
    }

    @Test
    void testGetExistingZip() {
        log.info "------ testGetExistingZip -------"
        assert (jz.zipfileMap.isEmpty())
        def cjz = jz.getCurrentJobZip(jobMessage)
        def map1 = cjz.zip
        assert (map1)
        assert (jz.zipfileMap[DEFAULT_JOB_ID] != null)
        cjz = jz.getCurrentJobZip(jobMessage)
        def map2 = cjz.zip
        assert (map2)
        assertEquals(map1, map2)
    }

    @Test
    void testInitializeZip() {
        log.info "------ testInitializeZip -------"
        assert (jz.zipfileMap.isEmpty())
        assert (jz.createZip(jobMessage))
        assert (jz.zipfileMap[DEFAULT_JOB_ID] != null)
    }

    @Test
    void testAddEntry() {
        log.info "------ testAddEntry -------"
        assert (jz.zipfileMap.isEmpty())
        def map = jz.createZip(jobMessage)
        assert (map)
        assert (map.numEntries == 0)
        assert (map.zip.exists())
        assert (map.zip.length() == 0)
        assert (jz.zipfileMap[DEFAULT_JOB_ID] != null)
        def mockS3Obj = new Expando()
        mockS3Obj.objectContent = [0x1]
        def cjz = jz.getCurrentJobZip(jobMessage)
        jz.addEntryToZip(cjz, "foo.bar", mockS3Obj)
        assert (map.zip.length() == 98) //this value discovered by testing
    }

    @Test
    void testFinishZip() {
        mockS3StorageService()
        mockFinishZip()
        log.info "------ testFinishZip -------"
        assert (jz.zipfileMap.isEmpty())
        def map = jz.createZip(jobMessage)
        assert (map)
        assert (map.zip.exists())
        assert (map.zip.length() == 0)
        assert (jz.zipfileMap[DEFAULT_JOB_ID] != null)
        def mockS3Obj = [objectContent: [0x1]]
        def cjz = jz.getCurrentJobZip(jobMessage)
        JobMessage jm = new JobMessage()
        cjz.jobMessage = jm
        jz.addEntryToZip(cjz, "foo.bar", mockS3Obj)
        assert (map.zip.length() == 98) //this value discovered by testing

        jz.finishZipUploadAndReport(cjz)
        unmockFinishZip()
        unmockS3StorageService()
    }

    /// @Test
    void testCreateZipFromJMS() {
        log.debug("----- testCreateZipFromJMS -----")
        mockS3StorageService()
        mockWatchdogTimer()
        def fakeConsumer = new Expando()
        fakeConsumer.setMessageListener = { it -> log.info "Using ${it} as a callback" }

        def session = [commit: { log.info "Committing a session" }, rollback: { log.info "Rolling back a session" }]


        jz.jmsLibWrapper = getMockJms()

        jz.setUpListeners()

        def cjz = jz.getCurrentJobZip(DEFAULT_JOB_ID)
        def map1 = cjz
        assert (map1)
        assert (map1.zip.exists())
        assert (map1.zip.length() == 0)

        def msg = createValidTaskRenderMessage()
        log.info "MSG: ${msg}"
        log.info "SENDING task MESSAGE.  Zip is ${map1.zip} and it exists: ${map1.zip.exists()}"
        cjz = jz.getCurrentJobZip(msg.object.jobId)
        jz.onMessage(msg)

        def map2 = cjz
        assertEquals(map1, map2)
        assert (map2)
        assert (map2.zip.exists())
        assertEquals(1, map2.numEntries)
        assert (map2.zip.length() == 56)
        log.info "For map2. Zip is ${map2.zip} and it exists: ${map2.zip.exists()}"

        msg = createValidMetadataMessage()
        log.info "Sending metadata message"
        jz.onMessage(msg)

        msg = createValidJobMessage()
        log.info "SENDING JOB DONE MESSAGE"
        jz.onMessage(msg)

        assert (cjz)
        assertNull(cjz.zip)
        unmockWatchdogTimer()
        unmockS3StorageService()
    }

    // @Test
    void testEmptyZip() {
        log.debug("----- testEmptyZip -----")
        mockWatchdogTimer()
        mockS3StorageService()
        def fakeConsumer = new Expando()
        fakeConsumer.setMessageListener = { it -> log.info "Using ${it} as a callback" }

        def session = [commit: { log.info "Committing a session" }, rollback: { log.info "Rolling back a session" }]


        jz.jmsLibWrapper = getMockJms()

        jz.setUpListeners()
        def cjz = jz.getCurrentJobZip(DEFAULT_JOB_ID)
        shouldFail(java.util.zip.ZipException) { jz.finishZipUploadAndReport(cjz) }
    }

    // @Test
    void testJobDoneFirst() {
        log.info "----- testJobDoneFirst -----"
        mockWatchdogTimer()
        mockS3StorageService()
        def fakeConsumer = new Expando()
        fakeConsumer.setMessageListener = { it -> log.info "Using ${it} as a callback" }

        def session = [commit: { log.info "Committing a session" }, rollback: { log.info "Rolling back a session" }]


        jz.jmsLibWrapper = getMockJms()

        jz.setUpListeners()
        def cjz = jz.getCurrentJobZip(DEFAULT_JOB_ID)
        def map1 = cjz
        assert (map1)
        assert (map1.zip.exists())
        assert (map1.zip.length() == 0)

        def msg = createValidJobMessage()
        log.info "SENDING JOB DONE MESSAGE"
        jz.onMessage(msg)
        cjz = jz.getCurrentJobZip(msg.object.jobId)
        def map2 = cjz
        assertEquals(map1, map2)
        assert (map2)
        assert (map2.zip.exists())
        assert (map2.zip.length() == 0)

        //We can't test a metadata message here because we can't unit-test threads.

        msg = createValidTaskRenderMessage()
        log.info "SENDING FIRST MESSAGE"
        jz.onMessage(msg)

        assertNull(cjz.zip)
        assertEquals(1, cjz.numEntries)
        unmockS3StorageService()
        unmockWatchdogTimer()
    }

    @Test
    void testWatchdogTimer() {
        log.info "----- testWatchdogTimer -----"
        mockFinishZip()
        def cjz = jz.getCurrentJobZip(jobMessage)
        def msg = createValidJobMessage()
        JobMessage jm = (JobMessage) JmsUtil.fromXML(msg.text)
        log.debug("Setting watchdog timer for 1 second")
        jz.resetWatchdogTimer(cjz, jm)
        log.debug("Waiting for 5 seconds")
        Thread.sleep(5000)
        unmockFinishZip()
    }

    void mockFinishZip() {
        log.info "Mocking out finishZip"
        JobZipper.metaClass.WATCHDOG_TIMEOUT = 1000
        JobZipper.metaClass.finishZipUploadAndReport = { log.info "Finishing zip file" }
    }

    void unmockFinishZip() {
        log.info "Unmocking finishZip"
        GroovySystem.metaClassRegistry.removeMetaClass(JobZipper.class)
    }
}
