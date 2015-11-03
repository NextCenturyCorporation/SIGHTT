
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

import grails.test.mixin.TestFor
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.util.WebUtils

import javax.servlet.ServletRequest

/**
 * @author tbrooks
 */

@TestFor(AuthController)
class AuthControllerTests {

    void testIndexRedirectToLogin() {
        controller.index()
        assert response.getRedirectedUrl() == "/auth/login"
    }

    void testLoginParams() {
        params.username = "tbrooks"
        params.targetUri = "/"

        def map = controller.login()

        assert map.get('username') == "tbrooks"
        assert map.get('rememberMe') == false
        assert map.get('targetUri') == "/"
    }

    void testSignInSuccessful() {
        params.username = "tbrooks"
        params.password = "password"
        params.targetUri = "/"

        mockSecurityManager()

        controller.signIn()
        assert response.getRedirectedUrl() == params.targetUri
    }

    void testSignInFailureRedirectsToLogin() {
        params.username = "tbrooks"
        params.password = "wrongPassword"
        params.targetUri = "/"

        mockSecurityManager()

        controller.signIn()
        assert response.getRedirectedUrl() == "/auth/login?username=tbrooks&targetUri=%2F"
    }

    void testUnauthorized() {
        controller.unauthorized()
        assert response.contentAsString
    }


    def mockSecurityManager() {
        WebUtils.metaClass.'static'.getSavedRequest = { ServletRequest request -> return null }

        SecurityUtils.metaClass.'static'.getSubject = {
            def service = mockFor(Subject)
            service.demand.login(1..1) {
                if (params.username != "tbrooks" || params.password != "password") {
                    throw new AuthenticationException()
                }

            }
            service.createMock()
        }

    }
}
