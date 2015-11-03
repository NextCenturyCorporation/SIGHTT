
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

import com.ncc.sightt.ComplexTaskProcessor
import com.ncc.sightt.RenderAndComposite
import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.ModelRenderMessage
import com.ncc.sightt.s3.FileStorageService
import com.ncc.sightt.s3.S3StorageService
import groovy.util.logging.Slf4j

@Slf4j
class ModelRenderMessageProcessor implements ComplexTaskProcessor<ModelRenderMessage> {

    def fileStorageConfiguration
    def storageService
    def modelBackgroundCache
    def renderer

    def processTask(ModelRenderMessage message) {

        log.info "Processing task:  ${JmsUtil.toXML(message)}"

//        // TODO:  Use configuration and dependency injection
//        FileStorageService storageService = new S3StorageService()
//        storageService.fileStorageConfiguration = fileStorageConfiguration

        long executionTimeStart = new Date().getTime()

        RenderAndComposite c = new RenderAndComposite()
        c.storageService = storageService
        c.cache = modelBackgroundCache
        c.renderer = renderer

        ModelRenderMessage finishedMessage = c.generateAndUploadModelView(message)
        finishedMessage.executionTimeStart = executionTimeStart
        finishedMessage.executionTimeEnd = new Date().getTime()
        log.info "Done processing task for ModelRenderMessage"

        // Send back a message with key for composite image
        JmsUtil.toXML(finishedMessage)
    }
}
