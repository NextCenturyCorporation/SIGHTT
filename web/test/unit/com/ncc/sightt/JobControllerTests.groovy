
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
import com.ncc.sightt.auth.User
import com.ncc.sightt.auth.UserAccountsService
import com.ncc.sightt.auth.UserPreferences
import grails.plugin.jms.JmsService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.subject.Subject

@TestFor(JobController)
@Mock([Job, JobConfig, Background, ObjectModel, JobTask, User, UserPreferences])
class JobControllerTests {

    def defaultOwner

    void setUp() {
        def userAccountService = [isAdmin: { return true }, currentUser: { name ->
            println "Current user: ${name}"
            return defaultOwner
        }] as UserAccountsService
        controller.userAccountsService = userAccountService
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()
        def passwordHash = new Sha512Hash("tester", passwordSalt, 1024).toHex()
        println "PASSWORD_HASH: ${passwordHash}"
        println "PASSWORD_SALT: ${passwordSalt}"
        defaultOwner = new User(username: "tester", email: "tester@example.com", passwordHash: passwordHash, passwordSalt: passwordSalt, preferences: new UserPreferences())
        if (!defaultOwner.save()) {

            println "ERROR HASH:${defaultOwner.passwordHash}"
            defaultOwner.errors.each {
                println it
            }
        }
    }

    def createValidJob(params) {
        populateValidParams(params)

        params.owner = defaultOwner
        params.permissions = Permissions.PRIVATE
        def job = new Job(params)
    }

    void populateValidParams(map) {
        assert map != null
        map.clear()
        map.jobName = "TestJob"
        map.owner = defaultOwner

        map.numComplete = 0
        map.numTasks = 1

        map.submitDate = new Date()
        map.expectedEndDate = new Date()
        map.actualEndDate = new Date()

        map
    }

    void populateValidJobConfigMap(map) {
        assert map != null
        map.clear()

        map.numImages = 1
        map.degreeSpacing = 2

        map
    }

    void testIndex() {
        controller.index()
        assert "/job/list" == response.redirectedUrl
    }

    void testList() {

        populateValidParams(params)
        def model = controller.list()

        assert model.jobInstanceList.size() == 0
        assert model.jobInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.jobInstance != null
    }

    void testSave() {
        /*
         * The metaClass stuff was taken from: http://stackoverflow.com/questions/1707830/using-groovy-metaclass-to-mock-out-shiro-securityutils-in-bootstrap
         */
        def suMetaClass = new ExpandoMetaClass(SecurityUtils)
        suMetaClass.'static'.getSubject = { [getPrincipal: { defaultOwner.username.toString() }, toString: { defaultOwner.username.toString() }] as Subject }
        suMetaClass.initialize()
        SecurityUtils.metaClass = suMetaClass

        def service = mockFor(TaskService)
        service.demand.generateTasks(1..1) {}
        controller.taskService = service.createMock()
        def jmsService = mockFor(JmsService)
        jmsService.demand.send(1..1) {}
        controller.jmsService = jmsService.createMock()

        params['job'] = [:]
        populateValidParams(params['job'])
        params['config'] = [:]
        populateValidJobConfigMap(params['config'])

        controller.save()

        assert flash.get("message")
        SecurityUtils.metaClass = null
    }

    void testShow() {
        def service = mockFor(ImageService)
        service.demand.getThumbnailSrcList(1..2) {}
        controller.imageService = service.createMock()
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/job/list'

        def job = createValidJob(params)
        populateValidJobConfigMap(params)
        def jobConfig = new JobConfig(params)
        job.config = jobConfig
        job.config.addToBackgrounds(new Background())
        job.config.addToObjectModels(new ObjectModel())
        assert job.save() != null

        def model = controller.show(job.id)

        assert model.jobInstance == job
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/job/list'

        def job = createValidJob(params)
        populateValidJobConfigMap(params)
        def jobConfig = new JobConfig(params)
        job.config = jobConfig

        assert job.save() != null

        def model = controller.edit(job.id)

        assert model.jobInstance == job
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/job/list'

        response.reset()

        def job = createValidJob(params)
        populateValidJobConfigMap(params)
        def jobConfig = new JobConfig(params)
        job.config = jobConfig

        assert job.save() != null

        controller.update()

        assert response.redirectedUrl == "/job/list"
        assert flash.get("message")
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/job/list'

        response.reset()

        def job = createValidJob(params)
        populateValidJobConfigMap(params)
        def jobConfig = new JobConfig(params)
        job.config = jobConfig

        assert job.save() != null
        assert Job.count() == 1

        controller.delete(job.id)

        assert Job.count() == 0
        assert Job.get(job.id) == null
        assert response.redirectedUrl == '/job/list'
    }
}
