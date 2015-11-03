
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

import com.ncc.sightt.auth.Permissions
import com.ncc.sightt.auth.Role
import com.ncc.sightt.auth.User
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash

import static org.junit.Assert.fail

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([User, Role, Thumbnail])
class BackgroundTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testCreate() {
        def params = [:]
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()

        params.owner = new User(username: "tester", passwordHash: new Sha512Hash("tester", passwordSalt, 1024), passwordSalt: passwordSalt)
        params.name = "testBackground"
        params.bucketName = "testBucket"
        params.filePath = "testPath"
        params.thumbnail = new Thumbnail()
        params.height = 1024
        params.width = 1024
        params.permissions = Permissions.PUBLIC

        def background = new Background(params)
        if (!background.validate()) {
            background.errors.each {
                println it
            }
            fail "background failed to validate"
        }
    }
}
