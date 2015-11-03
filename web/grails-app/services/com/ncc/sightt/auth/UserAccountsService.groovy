
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

import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.hash.Sha512Hash
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

class UserAccountsService {

    def mailService
    LinkGenerator grailsLinkGenerator

    /**
     * Parse a textual list of permissions into a list
     * @param text
     * @return
     */
    def parsePermissionsText(text) {
        def perms = []
        if (text != null) {
            text.eachLine {
                if (it != null) {
                    log.debug("Got permission: $it")
                    perms.add(it)
                }
            }
        }
        perms
    }

    /**
     * Convert a permissions list to text representation
     * @param perms
     * @return
     */
    def permsToText(perms) {
        def permissionsText = new StringBuffer()
        perms.each {
            permissionsText.append("$it\n")
        }
        permissionsText.toString()
    }

    /**
     * Create a password hash for the given user
     * @param password
     * @return
     */
    def hashPassword(password, passwordSalt) {
        log.debug("SALT: " + passwordSalt)
        def hash = new Sha512Hash(password, passwordSalt, 1024).toHex()
        log.debug("HASH: " + hash)
        hash
    }

    /**
     * Process a registration request and send the request to the administrators for authorization
     * @param pendingUser
     * @return whether or not the registration was successful
     */
    def registerNewUser(pendingUser) {
        boolean success = true
        def errors = []
        if (pendingUser.save()) {
            log.debug("Saved registration for: ${pendingUser}")

            sendRegistrationEmailToAdmin(pendingUser)
        } else {
            pendingUser.errors.each {
                errors.add(it)
            }
            success = false
        }
        [success: success, errors: errors]
    }

    def sendActivatedEmailToUser(user) {
        def loginLink = grailsLinkGenerator.link(controller: 'auth', action: 'login', absolute: true)
        mailService.sendMail {
            to "${user.email}"
            subject "SIGHTT Account Activated"
            html """<h1>Welcome to SIGHTT!</h1>
<p>Your account has been activated and is ready to use.</p>
<p>For your records, your username is: ${user.username}</p>
<p>Click <a href="${loginLink}">here</a> to login</p>
--SIGHTT Administrators"""
        }
    }

    /**
     * Send an email to the user to self-activate their pending registration
     * @param pendingUser
     * @return
     */
    def sendRegistrationEmailToUser(pendingUser) {
        def activationLink = grailsLinkGenerator.link(controller: 'registration', action: 'activate', id: pendingUser.activationCode, absolute: true)
        mailService.sendMail {
            to "${pendingUser.email}"
            subject "SIGHTT Registration"
            html """<h1>Welcome to SIGHTT!</h1>
<p>Your account has been registered and is ready to use.</p>

<p>For your records, your username is: ${pendingUser.username}</p>

<p>Your account is not yet active, please click the link below to activate your account:<br/>
<a href="$activationLink">Activate your account</a></p>

--SIGHTT Administrators"""
        }
    }

    /**
     * Send an email to the administrator with the registration request of the pending user
     * @param pendingUser
     */
    def sendRegistrationEmailToAdmin(pendingUser) {
        def activationLink = grailsLinkGenerator.link(controller: 'registration', action: 'activate', id: pendingUser.activationCode, absolute: true)
        def rejectActivationLink = grailsLinkGenerator.link(controller: 'registration', action: 'reject', id: pendingUser.activationCode, absolute: true)

        mailService.sendMail {
            to "registration@sightt.com"
            subject "SIGHTT Registration Request"
            html """A registration request for the following user has been created:
<table>
<tr><td>User:</td><td>${pendingUser.username}</td></tr>
<tr><td>Email:</td><td>${pendingUser.email}</td></tr>
<tr><td>IP:</td><td>${pendingUser.registrationIp}</td></tr>
</table>

<p>The following actions are available:
<ul style="list-style: none;">
<li><a href="$activationLink">Activate account</a></li>
<li><a href="$rejectActivationLink">Reject account</a></li>
</ul> </p>
<p>--SIGHTT Administrators</p>"""
        }
        log.debug("Mail sent to administrators!")
    }

    /**
     * Return the current user (or null if no user was found)
     * @return
     */
    def getCurrentUser() {
        User.find { username == SecurityUtils.subject.principal }
    }

    /**
     * Send an email to the user with instructions on how to reset their password
     * @param resetRequest
     */
    def sendPasswordResetEmail(resetRequest) {
        def passwordResetLink = grailsLinkGenerator.link(controller: 'auth', action: 'resetPassword', id: resetRequest.resetCode, absolute: true)
        mailService.sendMail {
            to "${resetRequest.user.email}"
            subject "Password Reset Request"
            html """<p>${resetRequest.user.username}, a password reset has been requested for your account.  To reset your password, please click <a href="$passwordResetLink">here</a></p>
<p>If you did not request this reset, please disregard this email</p>
<p>--SIGHTT Administrators</p>
"""
        }
        log.debug("Sent password reset email to user: ${resetRequest.user.username} email: ${resetRequest.user.email}")

    }

    /**
     * Change the password of the user given the current password and the new password.
     * Utilizes the password reset functionality
     * @param user
     * @param currentPassword
     * @param newPassword
     * @return
     */
    def changePassword(user, currentPassword, newPassword) {
        def currentHash = hashPassword(currentPassword, user.passwordSalt)
        if (currentHash != user.passwordHash) {
            return [error: "profile.invalid.current.password"]
        }
        log.debug("Changing users password")
        resetPassword(user, newPassword)
    }

    /**
     * Reset the password of the given user to the new password
     * @param user
     * @param newPassword
     * @return
     */
    def resetPassword(user, newPassword) {
        user.passwordHash = hashPassword(newPassword, user.passwordSalt)
        null
    }

    /**
     * Send an email to the user with the status of the given job
     * @param user
     * @param job
     */
    def sendJobStatusEmail(user, job) {
        mailService.sendMail {
            to user.email
            subject "Job ${job.name} on SIGHTT"
            switch (job.status) {

            }
            html """
"""
        }
    }

    boolean isAdmin() {
        def adminRole = Role.find { name == "admin" }
        currentUser.roles.contains(adminRole)
    }


    def updatePreferences(params) {
        if (params.group == "communication") {
            updateCommunicationPreferences(params)
        } else if (params.group == "job") {
            updateJobPreferences(params)
        }

    }

    def updateCommunicationPreferences(user, params) {
        log.debug("Updating Communication Preferences for user: ${user}")
        for (commPref in AllowedCommunications.values()) {
            if (params["${commPref}"] == "on") {
                log.debug("Setting commPref: ${commPref}")
                user.preferences.addToAllowedCommunications(commPref)
            } else {
                log.debug("UN-Setting commPref: ${commPref}")
                user.preferences.removeFromAllowedCommunications(commPref)
            }
        }
        user.preferences.save()
    }

    def updateCommunicationPreferences(grailsParameterMap) {
        updateCommunicationPreferences(currentUser, grailsParameterMap)
    }


    def updateJobPreferences(user, params) {
        log.debug("Updating job preferences")
        user.preferences.defaultPrivacy = params.jobPrivacy
    }

    def updateJobPreferences(params) {
        updateJobPreferences(currentUser, params)

    }

    /**
     * Check whether or not the user can access a given object. Makes checks a little easier
     * @param object in question
     * @return whether or not the current logged in user can access the given object
     */
    def currentUserCanRead(object) {
        ((object.owner == currentUser) || (object.permissions == Permissions.PUBLIC))
    }

    /**
     * Determines which objects are visible (based on any criteria in the objects themselves) and returns the list
     *
     * NOTE: This method will not return things like failed model uploads!
     * @param objectClass
     * @param params
     * @return
     */
    def findAllVisible(objectClass, params) {
        def visibleObjects
        if (objectClass.metaClass.getMetaMethod("visible")) {
            if (isAdmin()) {
                visibleObjects = objectClass.visible.list(params)
            } else {
                visibleObjects = objectClass.visible.findAllByOwnerOrPermissions(currentUser, Permissions.PUBLIC, params)
            }
        } else {
            if (isAdmin()) {
                visibleObjects = objectClass.list(params)
            } else {
                visibleObjects = objectClass.findAllByOwnerOrPermissions(currentUser, Permissions.PUBLIC, params)
            }
        }

        visibleObjects
    }
}
