
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
import com.ncc.sightt.auth.UserAccountsService
import com.ncc.sightt.s3.FileStorageService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import groovy.mock.interceptor.StubFor
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: abovill
 * Date: 5/21/14
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
@TestFor(WizardController)
@TestMixin(DomainClassUnitTestMixin)

class WizardControllerSpec extends Specification {
    def defaultOwner

    void setupSpec() {
        mockDomain(ObjectModel)
        mockDomain(Background)
    }

    void "testSetLimitedAspectsDefault"() {
        def existingModel = new ObjectModel(
                filePath: "/asdf/asdf/asdf",
                name: "Existing Model",
                thumbnail: new Thumbnail(),
                modelType: ModelType.OBJ)

        when:
        session.objectModels = [existingModel]
        controller.selectLimitedAspects()

        then:
        response.text == WizardController.SESSION_EXPIRED_TEXT
//        model?.modelLocation != null
    }

/*    void "testSelectBackgroundWithOneBackground"() {
        setup:
        mockDomain(Background, [[owner: defaultOwner, permissions: Permissions.PUBLIC, name: "Name1", filePath: "FilePath1",
                thumbnail: new Thumbnail(), geometry: []]])
        def userAccountService = Mock(UserAccountsService)
        def imageService = Mock(ImageService)
        def fileStorageService = Mock(FileStorageService)
        controller.imageService = imageService
        controller.userAccountsService = userAccountService
        controller.fileStorageService = fileStorageService


        when:
        def map = controller.selectBackground()

        then:
        with(userAccountService) {
            1 * userAccountService.findAllVisible(Background.class, null)
        }

        Background.count() == 1
        map.backgroundThumbnailSrcMap != null
        map.backgroundList == [background1]
        map.selectedBackground == null
    }*/


}
