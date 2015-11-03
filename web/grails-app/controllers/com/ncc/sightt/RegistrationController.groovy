
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

import com.ncc.sightt.auth.PendingUser
import com.ncc.sightt.auth.Role
import com.ncc.sightt.auth.User
import com.ncc.sightt.auth.UserPreferences
import org.apache.commons.lang3.RandomStringUtils
import org.apache.shiro.crypto.SecureRandomNumberGenerator

class RegistrationController {
    def userAccountsService
    def recaptchaService

    static allowedMethods = [validate: "POST", validateCaptcha: "POST"]

    def index() { render "Under Construction" }

    def register() {

    }

    def validateCaptcha() {
        log.debug "Validating Captcha!"
        /*
        Check that the captcha was correct...
         */
        if (!recaptchaService.verifyAnswer(session, request.getRemoteAddr(), params)) {
            //failed
            render(view: 'register', model: [params: params])
            return
        }

        params.passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()

        params.passwordHash = userAccountsService.hashPassword(params.password, params.passwordSalt)
        params.expiry = new Date() + 2 //2 days
        params.activationCode = RandomStringUtils.random(32, true, true)
        params.registrationIp = request.getHeader("X-Real-IP") ?: request.remoteHost


        def user = new PendingUser(params)
        def registrationResult = userAccountsService.registerNewUser(user)
        if (registrationResult['success'] == false) {
            flash.message = message(code: "registration.register.error", args: [registrationResult['errors']])
            return
        }
        log.debug("Registration completed for user: ${user}")
        [user: user]
    }


    def reject(String id) {
        def pendingUser = PendingUser.find { activationCode == id }
        if (pendingUser == null) {
            flash.message = message(code: "registration.reject.error", args: ["Activation code ${id} does not exist!"])
            return
        }
        pendingUser.delete()
        [user: pendingUser]
    }


    def activate(String id) {
        println "ActivationCode: ${id}"
        def pendingUser = PendingUser.find { activationCode == id }
        def user = null
        if (pendingUser != null) {
            println "ActivationCode provided for user: ${pendingUser.username}"
            //Check for expired activation
            if (pendingUser.expiry.before(new Date())) {
                flash.message = message(code: "registration.activation.error", args: ["Expired activation code"])
                return
            }
            //Create a real user based on the pending user
            def userProps = pendingUser.properties
            user = new User(userProps)
            user.preferences = new UserPreferences()
            def basicRole = Role.find { name == "user" }
            if (basicRole == null) {
                flash.message = message(code: "registration.activation.error", args: ["User role does not exist!"])
                return
            }
            user.addToRoles(basicRole)
            if (!user.save()) {
                log.debug("ERRORS saving user:")
                def errors = []
                user.errors.allErrors.each {
                    log.debug(it)
                    errors.add(it)
                }
                flash.message = message(code: "registration.activation.error", args: [errors])
                return
            }
            pendingUser.delete()
        } else {
            flash.message = message(code: "registration.activation.error", args: ["ActivationCode not found"])
            return
        }
        //Send email to user

        userAccountsService.sendActivatedEmailToUser(user)
        [user: user]
    }

    /**
     * Check to see if the desired username is already taken (AJAXified)
     * @param username
     * @return whether or not the username is already taken
     */
    def validate(username) {
        !User.list().contains(username)
    }
}
