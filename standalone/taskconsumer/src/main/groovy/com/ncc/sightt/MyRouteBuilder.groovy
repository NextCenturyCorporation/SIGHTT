
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

import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder

import com.ncc.sightt.message.ModelRenderMessage
import com.ncc.sightt.message.TaskRenderMessage

class MyRouteBuilder extends RouteBuilder {

    def incomingQ
    def taskRenderMessageQ
    def metadataMessageQ
    def modelRenderMessageQ
    def invalidQ

    void configure() {
        def route = from(incomingQ)
                .transacted()
                .beanRef("xmlToObjectConverter", "convert")
                .choice()
                .when { Exchange e -> e.in.body instanceof TaskRenderMessage }
                .beanRef("taskRenderMessageProcessor") //Uses only method as default
                .to("direct:taskSplitter")
                .when { Exchange e -> e.in.body instanceof ModelRenderMessage }
                .beanRef("modelRenderMessageProcessor")
                .to(modelRenderMessageQ)
                .otherwise()
                .to(invalidQ)
                .end()
        def taskSplitterRoute = from("direct:taskSplitter")
                .multicast()
                .to("direct:taskResponse", "direct:metadataResponse")
        def taskRoute = from("direct:taskResponse")
                .transform { it.in.body['compMsg'] }
                .to(taskRenderMessageQ)
        def metadataRoute = from("direct:metadataResponse")
                .transform { it.in.body['metaMsg'] }
                .to(metadataMessageQ)
    }

    /**
     * This is for when the processors are auto-loaded
     * @param route
     */
    void loadProcessors(route) {
        route
                .when { Exchange e -> e.in.body instanceof TaskRenderMessage }
                .process { log.info "***** Got task message" }
                .when { Exchange e -> e.in.body instanceof ModelRenderMessage }
                .process { log.info "***** Got render message" }
                .otherwise()
                .process { log.info "***** Got non-task message" }
    }
}
