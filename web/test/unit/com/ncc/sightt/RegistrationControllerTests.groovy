
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

import com.megatome.grails.RecaptchaService
import com.ncc.sightt.auth.*
import grails.plugin.mail.MailService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RegistrationController)
@Mock([User, PendingUser, Role, UserPreferences])
class RegistrationControllerTests {

    void setUp() {
        def userRole = new Role(name: "user", permissions: [""])
        userRole.save()
        def userAccountsService = [hashPassword: { pass, salt ->
            pass
        }] as UserAccountsService
        userAccountsService.mailService = [sendMail: {
            println "I sent an email!"
        }] as MailService
        userAccountsService.grailsLinkGenerator = [link: {
            "here is a link..."
        }] as LinkGenerator
        controller.userAccountsService = userAccountsService

        def recaptchaService = [verifyAnswer: { p1, p2, p3 ->
            log.info(p3.toString())
            return true
        }] as RecaptchaService
        controller.recaptchaService = recaptchaService
    }

    void populateValidParams(params) {
        assert params != null
        params.username = "tester"
        params.email = "tester@example.com"
        params.password = "tester"
    }

    void testRegisterSave() {
        populateValidParams(params)
        controller.request.addHeader("X-Real-Ip", "127.0.0.1")
        controller.validateCaptcha()
        assert User.list().size() == 0
        assert PendingUser.list().size() == 1
    }

    void testActivateAccount() {
        populateValidParams(params)
        controller.request.addHeader("X-Real-Ip", "127.0.0.1")
        def res = controller.validateCaptcha()
        println res.user
        controller.activate(res.user.activationCode)
        assert PendingUser.list().size() == 0
        assert User.list().size() == 1


    }

}
