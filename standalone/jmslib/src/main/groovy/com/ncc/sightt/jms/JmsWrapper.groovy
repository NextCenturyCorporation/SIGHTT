
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

import javax.jms.JMSException
import javax.jms.Session

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.command.ActiveMQTopic


/**
 * JmsWrapper aims to be a wrapper around the JMS API that enables rapid development of applications.
 * The goal is to provide a thread-safe manner for creating and managing connections to JMS brokers.
 *
 * It is currently hard-coded to use ActiveMQ, but in the future it may be possible to use spring to inject
 * the JMS dependencies at runtime
 * @author abovill
 *
 */
class JmsWrapper {
    def brokerUrl
    def connectionFactory
    def connection

    /**
     * Initialize a new JmsWrapper with the given broker URL and config
     * params config a string containing the URL parameters
     */
    def JmsWrapper(url, config) {
        config = config ? "?${config}" : ""
        String fullUrl = "${url}${config}"
        println "URL: ${fullUrl}"
        initJmsConnection(fullUrl)
    }

    def initJmsConnection(fullUrl) {
        connectionFactory = new ActiveMQConnectionFactory(fullUrl)
        connection = connectionFactory.createConnection()
        connection.start()
    }

    def getNewSession(transacted) {
        def session = connection.createSession(transacted, transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE)
        session
    }

    /**
     * Create a consumer for a given queue
     */
    def createConsumer(session, subject) {
        def destination
        try {
            destination = session.createQueue(subject)
        } catch (JMSException e) {
            println "Caught JMS Exception: ${e}"
        }
        def consumer = session.createConsumer(destination)
        consumer
    }

    def createSubscription(session, topic) {
        def destination
        try {
            destination = new ActiveMQTopic(topic)
        } catch (JMSException e) {
            println "Caught JMS Exception: ${e}"
        }
        def subscription = session.createConsumer(destination)
        subscription
    }

    def createPublisher(session, topic) {
        def destination
        try {
            destination = new ActiveMQTopic(topic)
        } catch (JMSException e) {
            println "Caught JMS Exception: ${e}"
        }
        def publisher = session.createProducer(destination)
        publisher
    }

    def createProducer(session, subject) {
        def destination
        try {
            destination = session.createQueue(subject)
        } catch (JMSException e) {
            println "Caught JMS Exception: ${e}"
        }
        def producer = session.createProducer(destination)
        producer
    }
}
