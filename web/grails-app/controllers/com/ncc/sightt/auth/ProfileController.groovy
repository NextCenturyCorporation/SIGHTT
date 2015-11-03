
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
package com.ncc.sightt.auth

import grails.converters.JSON
import org.apache.shiro.SecurityUtils

class ProfileController {
    def userAccountsService

    static allowedMethods = [changePassword: "POST", changeEmail: "POST", delete: "POST"]

    def index() {
        [user: User.find { username == SecurityUtils.subject.principal }]
    }

    def password() {

    }

    def changePassword() {
        def currentUser = userAccountsService.currentUser
        def ret = userAccountsService.changePassword(currentUser, params['currentPassword'], params['newPassword'])

        if (ret != null) {
            if (ret?.error != null) {
                flash.message = message(code: ret.error)
                redirect([action: "password"])
                return
            } else {
                flash.message = message(code: "profile.generic.password.error")
                redirect([action: "password"])
                return
            }
        }

    }

    def email() {
        def currentUser = userAccountsService.getCurrentUser()
        log.debug("Current email: ${currentUser.email}")
        [currentEmail: currentUser.email]
    }

    def changeEmail() {
        def currentUser = userAccountsService.currentUser
        currentUser.email = params['email']
        [currentEmail: currentUser.email]

    }

    def viewBackgrounds() {

    }

    def viewModels() {

    }

    def preferences() {
        if (request.method == "POST") {
            log.debug("POST HAPPENED: ${params}")
            userAccountsService.updatePreferences(params)
            flash.message = message(code: "preferences.update.successful", args: ["User"])

        }
        //Display page
        User user = userAccountsService.currentUser
        log.debug("DEFAULT PRIVACY: ${user.preferences as JSON}")
        [user: userAccountsService.currentUser]
    }
}
