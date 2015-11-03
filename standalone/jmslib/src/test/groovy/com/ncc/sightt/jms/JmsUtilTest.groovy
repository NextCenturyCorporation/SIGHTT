
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

import groovy.util.logging.*

import org.junit.Assert

import com.ncc.sightt.message.TaskRenderMessage


class JmsUtilTest extends GroovyTestCase {

    static final String BACKGROUNDKEY = "s3/bucket/blah"
    static final double YAW = -2334.353425
    static final int EXITVALUE = 3

    void testConstructor() {
        TaskRenderMessage tm = new TaskRenderMessage()
        tm.backgroundKey = BACKGROUNDKEY
        tm.yaw = YAW
        tm.stderr = "thsi si sthe std err message"
        tm.exitValue = EXITVALUE
        String xml = JmsUtil.toXML(tm)
        println xml

        Object fromXML = JmsUtil.fromXML(xml)
        if (fromXML instanceof TaskRenderMessage) {
            TaskRenderMessage tm2 = (TaskRenderMessage) fromXML
            println "TaskRenderMessage: ${tm2}"
            Assert.assertEquals("Wrong background key", BACKGROUNDKEY, tm2.backgroundKey)
            Assert.assertEquals("Wrong yaw value", YAW, tm2.yaw, 0.0001)
            Assert.assertEquals("Wrong exit value", EXITVALUE, tm2.exitValue)
        } else {
            println "Type of object ${fromXML.class}"
            Assert.fail("wrong type of message deserialized from XML")
        }
    }
}
