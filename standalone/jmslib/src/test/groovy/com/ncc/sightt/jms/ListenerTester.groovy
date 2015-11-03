
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
package com.ncc.sightt.jms

import javax.jms.Message
import javax.jms.MessageListener
import javax.jms.TextMessage

import com.ncc.sightt.message.TaskRenderMessage


class ListenerTester implements MessageListener {

    def producerSession
    def consumerSession

    // public static final String JMSURL = 'vm://localhost:61616'
    public static final String JMSURL = 'tcp://broker.sightt.org:61616'

    def listen() {
        JmsWrapper jmsWrapper = JmsWrapperFactory.createJmsWrapper(JMSURL)
        producerSession = jmsWrapper.getNewSession(true)
        consumerSession = jmsWrapper.getNewSession(true)

        def rTaskSubscription = jmsWrapper.createSubscription(consumerSession, 'test.remoteTaskDone')
        rTaskSubscription.setMessageListener(this)

        this.sleep(1000)

        def rProducer = jmsWrapper.createPublisher(producerSession, 'test.remoteTaskDone');
        Message message1 = producerSession.createTextMessage("0")
        Message message2 = producerSession.createTextMessage("1")

        TaskRenderMessage test = new TaskRenderMessage()

        for (int i = 1; i <= 10; i++) {
            try {
                test.bucketName = "sightt-test"
                test.jobId = i % 2
                test.compositeKey = "test-do-not-delete/${i}.png"

                Message objMessage = producerSession.createTextMessage(JmsUtil.toXML(test))
                println "Sending message ${i}"
                rProducer.send(objMessage)
            }
            catch (Throwable e) {
                println("Got exception: ${e}")
            }
        }
        rProducer.send(message1)
        rProducer.send(message2)
        producerSession.commit()
    }

    void onMessage(Message message) {
        println("Message received: ${message}")
        if (message instanceof TextMessage) {
            println "${message.text}"
        }
        consumerSession.commit()
    }

    public static void main(String[] args) {
        ListenerTester l = new ListenerTester()
        l.listen()

        println("Got to the end")
    }
}
