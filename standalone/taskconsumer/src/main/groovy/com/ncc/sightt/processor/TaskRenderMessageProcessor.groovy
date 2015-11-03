
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
package com.ncc.sightt.processor

import groovy.util.logging.Slf4j

import com.ncc.sightt.ComplexTaskProcessor
import com.ncc.sightt.RenderAndComposite
import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.MetadataMessage
import com.ncc.sightt.message.TaskRenderMessage
import com.ncc.sightt.s3.FileStorageService
import com.ncc.sightt.s3.S3StorageService
import com.ncc.sightt.s3.Utils

@Slf4j
class TaskRenderMessageProcessor implements ComplexTaskProcessor<TaskRenderMessage> {

    def fileStorageConfiguration
    def storageService
    def modelBackgroundCache
    def renderer

    def processTask(TaskRenderMessage taskInfo) {
        log.info "Processing task:  ${JmsUtil.toXML(taskInfo)}"

//        // TODO:  Use configuration and dependency injection
//        FileStorageService storageService = new S3StorageService()
//        storageService.fileStorageConfiguration = fileStorageConfiguration

        long executionTimeStart = new Date().getTime()

        RenderAndComposite renderAndComposite = new RenderAndComposite()
        renderAndComposite.storageService = storageService
        renderAndComposite.cache = modelBackgroundCache
        renderAndComposite.renderer = renderer
        TaskRenderMessage finishedMessage = renderAndComposite.generateAndUploadSceneWithBlender(taskInfo)
        finishedMessage.executionTimeStart = executionTimeStart
        finishedMessage.executionTimeEnd = new Date().getTime()
        log.info "Done processing task:  ${JmsUtil.toXML(finishedMessage)}"

        // Send the metadata
        MetadataMessage metaMsg = new MetadataMessage()
        metaMsg.jobId = finishedMessage.jobId
        metaMsg.jobName = finishedMessage.jobName
        metaMsg.metadataFilename = "metadata/" + Utils.getFilenameFromFullFilepath(finishedMessage.compositeKey ?: "") + ".xml"
        metaMsg.metadata = finishedMessage.metadata
        metaMsg.error = finishedMessage.error

        log.info "Done creating metadata message:  ${JmsUtil.toXML(metaMsg)}"
        [compMsg: JmsUtil.toXML(finishedMessage), metaMsg: JmsUtil.toXML(metaMsg)]
    }
}
