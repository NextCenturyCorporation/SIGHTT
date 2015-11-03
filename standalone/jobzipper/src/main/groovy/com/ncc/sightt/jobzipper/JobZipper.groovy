
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

import groovy.util.logging.Log4j

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

import javax.jms.Message
import javax.jms.MessageConsumer
import javax.jms.MessageListener
import javax.jms.Session
import javax.jms.TextMessage

import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.jms.JmsWrapper
import com.ncc.sightt.jms.JmsWrapperFactory
import com.ncc.sightt.message.JobMessage
import com.ncc.sightt.message.MetadataMessage
import com.ncc.sightt.message.TaskRenderMessage
import com.ncc.sightt.s3.Utils


@Log4j
class JobZipper implements MessageListener {

    def static final DEFAULT_ZIP_DIR = "/mnt/sightt-zips"
    def static final JOB_DONE_QUEUE_RECV = "sightt.task.zipTask"
    def static final ZIP_FILE_DONE_QUEUE_SEND = "sightt.task.zipFileDone"

    static final WATCHDOG_TIMEOUT = 60000
    // JMS, Consumers, producers, sessions
    def jmsUrl
    def jmsConfig
    JmsWrapper jmsLibWrapper

    def zipTaskSession
    MessageConsumer zipTaskConsumer

    Session zipDoneSession
    def zipDoneProducer

    // Zip information
    def zipfileMap  // map between jobs and zips
    //  def currentJobZip
    def zipFileStorageDir

    // Network storage
    def storageService

    String imageExtension = ".png"

    //Watchdog Timer
    def watchdogTimer

    static zipWatchdogTask = { finishCall, currentJobZip ->
        log.info "Watchdog timer for ${currentJobZip} fired!"
        try {
            finishCall(currentJobZip)
        } catch (ZipException e) {
            log.warn("There were no entries in the zip file, cannot finish it.  We can only " +
                    "hope something else triggers another watchdog timer")
        }
    }

    /**
     * CTOR
     */
    def JobZipper() {
        watchdogTimer = new Timer("ZipDoneWatchdog")
        createZipDir()
    }

    // -------------------------------------------------------------
    // Initialization functions
    // -------------------------------------------------------------
    def initializeAndListen() {
        createZipMap()
        setUpListeners()
    }

    def createZipMap() {
        def tempMap = [:]
        zipfileMap = Collections.synchronizedMap(tempMap)
    }

    def setUpListeners() {
        log.info "Preparing to process zip tasks!"

        jmsLibWrapper = JmsWrapperFactory.createJmsWrapper(jmsUrl, jmsConfig)

        zipTaskSession = jmsLibWrapper.getNewSession(true)
        zipTaskConsumer = jmsLibWrapper.createConsumer(zipTaskSession, JOB_DONE_QUEUE_RECV)
        zipTaskConsumer.setMessageListener(this)

        zipDoneSession = jmsLibWrapper.getNewSession(true)
        zipDoneProducer = jmsLibWrapper.createProducer(zipDoneSession, ZIP_FILE_DONE_QUEUE_SEND)
    }

    def createZipDir() {
        def zipFileDir = new File(DEFAULT_ZIP_DIR)
        if (!zipFileDir.exists() && !zipFileDir.mkdir()) {
            def tmpDir = System.getProperty("java.io.tmpdir")
            def storeDir = new File("${tmpDir}/sightt-zips")
            storeDir.mkdir()
            zipFileStorageDir = storeDir
            log.warn "Could not create ${DEFAULT_ZIP_DIR}. Reverting to ${zipFileStorageDir}"
        } else {
            //directory exists
            zipFileStorageDir = zipFileDir
        }
    }

    // -------------------------------------------------------------
    // Message handling
    // -------------------------------------------------------------
    @Override
    void onMessage(Message message) {
        log.debug "Got a message: ${message}"
        try {
            handleMessage(message)
        } catch (Throwable t) {
            log.warn "Throwable caught: ${t} ${t.getStackTrace()}"
        }
    }

    void handleMessage(Message message) {

        if (message instanceof TextMessage) {
            log.debug "Got Text Message: ${message.text}"
            Object messageObject = JmsUtil.fromXML(message.text)

            if (messageObject instanceof TaskRenderMessage) {
                log.debug("processing task message")
                processTaskRenderMessage(messageObject)
                log.debug("finished processing task message")
            } else if (messageObject instanceof JobMessage) {
                log.debug("processing job message")
                processJobMessage(messageObject)
                log.debug("finished processing job message")
            } else if (messageObject instanceof MetadataMessage) {
                log.debug("processing metadata message")
                processMetadataMessage(messageObject)
                log.debug("finished processing metadata message")
            } else {
                log.info "Unknown type of TextMessage: ${messageObject}.  Text is: ${message.text}"
            }
            zipTaskSession.commit()
            return
        }

        // If got to here, then was not text or object message
        log.info "Unrecognized message: ${message}"
    }

    /**
     * Called when a Task has finished and there is a composite that should be added to the zip file.
     * Also adds the mask file to the zip!
     */
    void processTaskRenderMessage(TaskRenderMessage taskRenderMessage) {
        def currentJobZip = getCurrentJobZip(taskRenderMessage)

        if (taskRenderMessage.error.isEmpty()) {
            // Add composite file to zip
            log.info "Got Task Message. Task finished for job ${taskRenderMessage.jobId}"
            def compositeStream = storageService.retrieveObjectFromStoreAsStream(taskRenderMessage.compositeKey)
            log.debug "Adding composite to zip.  composite key: ${taskRenderMessage.compositeKey}"
            addEntryToZip(currentJobZip, taskRenderMessage.compositeKey, compositeStream)

            // Add mask file to zip
            def maskFilename = getMaskFilenameFromFullFilepath(taskRenderMessage.compositeKey)
            def maskStream = storageService.retrieveObjectFromStoreAsStream(taskRenderMessage.maskKey)
            def newMaskEntryName = "mask/" + maskFilename
            addEntryToZip(currentJobZip, newMaskEntryName, maskStream)

            // Add mask parts to zip
            for (int ii = 0; ii < taskRenderMessage.partMaskKeys.size(); ii++) {
                String partMaskKey = taskRenderMessage.partMaskKeys.get(ii)
                String partFilename = taskRenderMessage.partMaskImageFilenames.get(ii)
                def partMaskStream = storageService.retrieveObjectFromStoreAsStream(partMaskKey)
                def partNewMaskEntryName = "mask/" + getPartMaskFilenameFromFullFilepath(taskRenderMessage.compositeKey, partFilename)
                addEntryToZip(currentJobZip, partNewMaskEntryName, partMaskStream)
            }
        } else {
            log.warn "Job ${taskRenderMessage.jobId} failed; not adding to zip."
        }

        currentJobZip.numEntries++

        // Check to see if this job is done and we have added the last task
        if (currentJobZip.jobMessage) {
            log.debug("Current job is marked as finished, processTaskRenderMessage is resetting watchdog timer")

            resetWatchdogTimer(currentJobZip, currentJobZip.jobMessage)
        }
        log.info "Num Entries: ${currentJobZip.numEntries} done and added to zip"
    }

    def getCurrentJobZip(message) {
        def jobId = message.jobId
        def currentJobZip = zipfileMap[jobId]
        if (currentJobZip == null) {
            currentJobZip = createZip(message)
        }
        currentJobZip
    }

    def createZip(message) {
        def jobId = message.jobId
        def zipFile = File.createTempFile("temp_job_${jobId}_", ".zip", zipFileStorageDir)
        def fOut = new FileOutputStream(zipFile)
        log.debug "Created a new zipfile for Job ${jobId} at: ${zipFile.path}"
        String jobName = message.jobName
        zipfileMap[message.jobId] = [jobId: jobId, jobName: jobName, zip: zipFile, stream: new ZipOutputStream(fOut), numEntries: 0,
                numMetadata: 0, jobMessage: null, watchdogTask: null]
        zipfileMap[jobId]
    }

    def addEntryToZip(currentJobZip, entryName, compositeStream) {
        String entryFullName = getZipBaseName(currentJobZip) + "/" + entryName
        currentJobZip.stream.putNextEntry(new ZipEntry(entryFullName))
        currentJobZip.stream << compositeStream
        currentJobZip.stream.closeEntry()
    }

    /**
     * Called when Job has finished (TaskService says all tasks have finished) so close the zip and report, except
     * when not all the tasks have been received
     */
    void processJobMessage(jobMessage) {
        def currentJobZip = getCurrentJobZip(jobMessage)
        log.debug "Got a jobDone message.  Job finished for job ${jobMessage.jobId}"
        currentJobZip.jobMessage = jobMessage
        resetWatchdogTimer(currentJobZip, jobMessage)
    }

    /**
     * Determine whether we have received all of the jobs or not and can finish the zip
     * If not, start (or restart) the watchdog timer
     * @param currentJobZip
     * @param jobMessage
     * @return
     */
    def resetWatchdogTimer(currentJobZip, jobMessage) {
        def needToWait = false

        log.debug("Resetting the watchdot timer")
        if (jobMessage.numTasks > currentJobZip.numEntries) {
            log.info "Not all task messages received, ${jobMessage.numTasks} in job, but only ${currentJobZip.numEntries} entries.  waiting"
            needToWait = true
        }

        if (jobMessage.numTasks > currentJobZip.numMetadata) {
            log.info "Not all metadata messages received,${jobMessage.numTasks} in job, but only ${currentJobZip.numMetadata} entries waiting"
            needToWait = true
        }

        if (currentJobZip.watchdogTask) {
            currentJobZip.watchdogTask.cancel()
            currentJobZip.watchdogTask = null
        }
        //Set a timeout of 1 second if we already have everything
        def timeout = needToWait ? WATCHDOG_TIMEOUT : 1000
        currentJobZip.watchdogTask = watchdogTimer.runAfter(timeout, {-> zipWatchdogTask(this.&finishZipUploadAndReport, currentJobZip) })
        log.debug("Watchdog timer expected to fire at ${currentJobZip.watchdogTask.scheduledExecutionTime()}")
    }

    def finishZipUploadAndReport(currentJobZip) {
        currentJobZip.stream.finish()
        uploadZip(currentJobZip)
        reportZipPath(currentJobZip)
        cleanupZip(currentJobZip)

    }

    def uploadZip(currentJobZip) {
        String zipBaseName = getZipBaseName(currentJobZip)
        String prefix = "zips/${zipBaseName}_"
        def fileKey = storageService.saveFileToStore(prefix, currentJobZip.zip, "zip")
        log.debug("Uploaded the zipfile to ${fileKey}")
        currentJobZip.jobMessage.zipFilePath = fileKey
        currentJobZip.jobMessage.zipFileSize = currentJobZip.zip.size()
    }

    def reportZipPath(currentJobZip) {
        log.debug "Sending message about zip completed"
        def retMessage = zipDoneSession.createTextMessage(JmsUtil.toXML(currentJobZip.jobMessage))
        zipDoneProducer.send(retMessage)
        zipDoneSession.commit()
    }

    def cleanupZip(currentJobZip) {
        try {
            currentJobZip.zip.delete()
            currentJobZip.zip = null
        } catch (Exception e) {
            log.warn "Caught exception while cleaning up the zipfile"
            e.printStackTrace()
        }
    }

    def processMetadataMessage(MetadataMessage metadataMessage) {
        def currentJobZip = getCurrentJobZip(metadataMessage)
        currentJobZip.numMetadata++

        if (!metadataMessage.error.isEmpty()) {
            log.warn "Metadata message contains an error; not adding to zip job."
            return
        }

        def base = getZipBaseName(currentJobZip) + "/"
        def entryName = base + metadataMessage.metadataFilename
        InputStream is = new ByteArrayInputStream((metadataMessage.metadata).getBytes());

        currentJobZip.stream.putNextEntry(new ZipEntry(entryName))
        currentJobZip.stream << is
        currentJobZip.stream.closeEntry()

        // If the job done message has been received, then see if we are now finished
        if (currentJobZip.jobMessage) {
            log.debug("Current job is marked as finished, processMetadataMessage is resetting watchdog timer")
            resetWatchdogTimer(currentJobZip, currentJobZip.jobMessage)
        }
    }

    // --------------------------------------------------------------------------------
    // Name Mangling Functions
    // FIXME: These should go in a util class and be broken out into a new dependency
    // --------------------------------------------------------------------------------

    String getZipBaseName(currentJobZip) {
        log.debug "CurrentJobZip ${currentJobZip}"
        String base = "${currentJobZip?.jobName}_${currentJobZip?.jobId}"
        return base
    }

    String getMaskFilenameFromFullFilepath(String fullFilepath) {
        String compositeName = getCompositeName(fullFilepath)
        String filename = compositeName + ".mask" + imageExtension
        return filename
    }

    String getPartMaskFilenameFromFullFilepath(String fullFilepath, String partFullFilename) {
        // Get the part name
        String partFilename = Utils.getFilenameFromFullFilepath(partFullFilename)
        String partName = Utils.getModelName(partFilename)
        String compositeName = getCompositeName(fullFilepath)
        String filename = compositeName + ".mask." + partName + imageExtension
        return filename
    }

    String getCompositeName(String fullFilepath) {
        int slashIndex = fullFilepath.lastIndexOf("/")
        if (slashIndex == -1 || slashIndex == fullFilepath.length() - 1) {
            return fullFilepath
        }

        def dotIndex = fullFilepath.lastIndexOf(".")
        if (dotIndex > 0) {
            def compositeName = fullFilepath.substring(slashIndex + 1, dotIndex)
            imageExtension = fullFilepath.substring(dotIndex)
            return compositeName
        }

        log.warn "Unable to get desired index of last dot from filename ${fullFilepath}"
        return compositeName
    }
}
