
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
import com.ncc.sightt.*
import com.ncc.sightt.auth.Permissions
import com.ncc.sightt.auth.Role
import com.ncc.sightt.auth.User
import com.ncc.sightt.auth.UserPreferences
import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.ModelRenderMessage
import grails.util.Environment
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash

import javax.imageio.ImageIO
import javax.jms.TextMessage
import javax.servlet.ServletContext
import java.awt.image.BufferedImage

class BootStrap {

    def messageSource
    def imageService
    def fileStorageService
    def fileStorageConfiguration
    def grailsApplication
    def dynamicComputeResourceTimerService
    def jmsService

    def adminUser
    def adminRole
    def userRole
    def envname
    def init = { ServletContext servletContext ->
        initializeRoles()
        initializeAdminUser()

        log.info("Current environment name: ${Environment.current.name}")
        Environment.executeForCurrentEnvironment {
            production {
                envname = "production"
                resetPassword()
                loadBlankBackground()
                initializeDynamicComputeResourcesTimer()
            }
            sighttdev {
                envname = "sighttdev"
                resetPassword()
                loadBlankBackground()
                initializeDynamicComputeResourcesTimer()
            }
            development {
                envname = "development"
                loadDefaultData()
                initializeDevelopmentUsers()
                // DO NOT create new dynamic compute nodes in dev
                // initializeDynamicComputeResourcesTimer()
            }
        }
        log.info("filestorageconfig: ${fileStorageConfiguration?.frontendURL}")


    }

    def initializeRoles() {
        adminRole = Role.findByName("admin")
        if (adminRole == null) {
            adminRole = new Role(name: "admin", permissions: ["*:*"])
            if (!adminRole.save()) {
                log.warn "Saving user role failed:"
                adminRole.errors.each { log.warn it }
            }
        } else if (!adminRole.permissions.contains("*:*")) {
            adminRole.addToPermissions("*:*")
            if (!adminRole.save()) {
                log.warn "Saving user role failed:"
                adminRole.errors.each { log.warn it }
            }
        }


        userRole = Role.findByName("user")
        if (userRole == null) {
            userRole = new Role(name: "user")
        }
        def defaultUserPerms = ["objectModel:list",
                "background:thumbImgSrc",
                "objectModel:index",
                "job:index",
                "job:show",
                "job:save",
                "objectModel:show",
                "background:list",
                "background:index",
                "background:save",
                "job:list",
                "background:show",
                "wizard:*",
                "imageDisplay:*",
                "profile:*",
                "objectModel:create",
                "objectModel:save",
                "objectModel:thumbImgSrc",
                "background:togglePermissions",
                "objectModel:togglePermissions"]
        defaultUserPerms.each { userRole.addToPermissions(it) }
        if (!userRole.save()) {
            log.warn "Saving user role failed:"
            userRole.errors.each { log.warn it }
        }
    }


    def initializeAdminUser() {
        adminUser = User.findByUsername("sightt")
        if (adminUser == null) {
            def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()
            adminUser = new User(username: "sightt", email: "admin@sightt.com",
                    passwordHash: new Sha512Hash("sightt", passwordSalt, 1024).toHex(), passwordSalt: passwordSalt, preferences: new UserPreferences())
            adminUser.addToRoles(adminRole)
            log.debug("ADMIN HASH: " + adminUser.passwordHash)
            if (!adminUser.save()) {
                log.warn "Saving user ${adminUser.username} failed:"
                adminUser.errors.each { log.warn it }
            }
        } else {
            log.debug("Admin user already exists!")
        }
    }

    def initializeDevelopmentUsers() {
        def passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()
        def devUser = new User(username: "dev", email: "dev@sightt.com", passwordHash: new Sha512Hash("dev", passwordSalt, 1024).toHex(), passwordSalt: passwordSalt, preferences: new UserPreferences())
        devUser.addToRoles(userRole)
        log.debug("DEV HASH: " + devUser.passwordHash)
        if (!devUser.save()) {
            log.warn "Saving user ${devUser.username} failed:"
            devUser.errors.each { log.warn it }
        }
    }


    def initializeDynamicComputeResourcesTimer() {
        dynamicComputeResourceTimerService.initialize()
    }


    def destroy = {
    }

    def resetPassword() {
        def user = User.findByUsername("sightt")
        def origHash = new Sha512Hash("sightt", user.passwordSalt, 1024).toHex()
        if (origHash == user.passwordHash) {
            user.passwordSalt = new SecureRandomNumberGenerator().nextBytes().getBytes()
            user.passwordHash = new Sha512Hash("sightt9501", user.passwordSalt, 1024).toHex()

            if (!user.save()) {
                log.warn "Saving user ${user.username} failed:"
                user.errors.each { log.warn it }
            }
        }
        log.debug("Production users")
        User.all.each {
            log.debug("User: ${it.username} =>\nHash: ${it.passwordHash}\nSalt: ${it.passwordSalt}")
        }
    }

    def loadDefaultBackground() {
        // Save background image
        log.debug("Saving background image  office_small.jpg to store")
        def mainImageFilename = "test/resources/images/office_small.jpg"
        File imageFile = new File(mainImageFilename)
        def backgroundInfo = fileStorageService.bootStrapStoreBackground(imageFile)

        // Create a background domain object
        log.debug("Creating background object")
        def thumb = new Thumbnail()
        if (!thumb.save(flush: true)) {
            log.warn "Saving thumbnail for default background failed:"
            thumb.errors.each { log.warn it }
        }
        def background = new Background(owner: adminUser, permissions: Permissions.PUBLIC, name: "Office", filePath: backgroundInfo['filePath'],
                width: backgroundInfo['width'], height: backgroundInfo['height'], thumbnail: thumb)
        def thumbImg = imageService.generateAndStoreThumbnailFromStoredImage(background.thumbnail.id, backgroundInfo['filePath'])
        if (!background.save(flush: true)) {
            log.error "Saving default background failed:"
            background.errors.each { log.error it }
        }
    }

    def loadBlankBackground() {
        log.info("Loading blank background")
        def final BLANK_BACKGROUND_NAME = "DEFAULT_BLANK_BACKGROUND_800x600"
        def query = Background.where { name == BLANK_BACKGROUND_NAME }
        Background blank = query.find()
        if (!blank) {
            //Create a blank background
            def mainImageFile = grailsApplication.mainContext.getResource("bootstrap/blank-800x600.png").file
            def backgroundInfo = fileStorageService.bootStrapStoreBackground(mainImageFile)
            log.info "BackgroundInfo: ${backgroundInfo}"


            def mainThumbFile = grailsApplication.mainContext.getResource("bootstrap/blank-thumb.png").file
            def bufImg = ImageIO.read(mainThumbFile)
            def thumbPath = fileStorageService.saveThumbnailImage(bufImg, "blank")

            // Create a background domain object
            log.debug("Creating background object")
            def thumb = new Thumbnail()
            thumb.filePath = thumbPath
            thumb.valid = true
            if (!thumb.save(flush: true)) {
                log.warn "Saving thumbnail failed:"
                thumb.errors.each { log.warn it }
            }
            def background = new Background(owner: adminUser, permissions: Permissions.PUBLIC, name: BLANK_BACKGROUND_NAME, filePath: backgroundInfo['filePath'],
                    width: backgroundInfo['width'], height: backgroundInfo['height'], thumbnail: thumb)

            if (!background.save(flush: true)) {
                log.error "Saving blank background failed:"
                background.errors.each { log.error it }
            }
        }
        log.debug("Finished creating blank background")
    }

    def launchModelImport(def modelName, def modelFile) {
        log.debug("Starting process of importing model: ")

        def params = [:]

        def modelPath = fileStorageService.storeUploadedModel(new File(modelFile))

        params['name'] = modelName
        params['modelType'] = ModelType.BLENDER
        params['imageFilePath'] = null
        params['objFilePath'] = null
        params['bucketName'] = fileStorageService.fileStorageConfiguration.bucketName
        params['status'] = ModelStatus.PENDING
        params['filePath'] = modelPath

        def thumb = new Thumbnail()
        if (!thumb.save()) {
            log.warn "Saving thumbnail for ${params.name} failed:"
            thumb.errors.each { log.warn it }
            return
        }

        params['thumbnail'] = thumb
        params['owner'] = adminUser
        params['permissions'] = Permissions.PUBLIC

        def objectModelInstance = new ObjectModel(params)
        if (!objectModelInstance.save(flush: true)) {
            log.warn "Saving object model ${params.name} failed:"
            objectModelInstance.errors.each { log.warn it }
            return
        }

        def renderModelMsg = new ModelRenderMessage()
        renderModelMsg.objectModelId = objectModelInstance.id
        renderModelMsg.bucketName = fileStorageService.fileStorageConfiguration.bucketName
        renderModelMsg.modelKey = modelPath
        renderModelMsg.generateObjectFile = true
        // We need to emulate three.js, which sets the angles for the
        // rest of the application.  From the point of view of three.js,
        // the 'side' view is at -90 degrees.  
        renderModelMsg.yaw = -(Math.PI/2.0)

        TextMessage renderModelMsgAsTextMessage = JmsUtil.getMessageAsTextMessage(renderModelMsg)

        log.debug("Sending jms message")
        jmsService.send(service: 'task', method: 'runTask', renderModelMsgAsTextMessage)
    }

    def loadDefaultData() {
        log.warn(" file storage service is ${fileStorageService}")
        log.warn(" image service is ${imageService}")
        loadDefaultBackground()
        loadBlankBackground()

        // Add the default models
        launchModelImport("RPG-7", "test/resources/images/GuillaumeCote_rpg-7.blend")
        launchModelImport("coffeemug", "test/resources/images/coffeeMug.blend")
        launchModelImport("bucket", "test/resources/images/BucketBGE.blend")
        launchModelImport("hazmat", "test/resources/images/Drum_HP.blend")
        launchModelImport("handgun", "test/resources/images/Handgun_Game_Cycles.blend")
        launchModelImport("teacup", "test/resources/images/teacup.blend")
        launchModelImport("silverball", "test/resources/images/ball.blend")
    }
}
