
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
import com.ncc.sightt.jms.JmsUtil
import com.ncc.sightt.message.ModelRenderMessage
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

import javax.jms.TextMessage

class ObjectModelController {

    def jmsService
    def fileStorageService
    def imageService
    def userAccountsService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def validModels
        if (params.owned == "true") {
            validModels = ObjectModel.visible.findAllByOwner(userAccountsService.currentUser)
        } else if (userAccountsService.isAdmin()) {
            validModels = ObjectModel.list(params)
        } else {
            validModels = ObjectModel.visible.findAllByOwnerOrPermissions(userAccountsService.currentUser, Permissions.PUBLIC, params)
        }
        Map thumbSrcMap = imageService.getThumbnailSrcList(validModels)


        [objectModelInstanceList: validModels, objectModelInstanceTotal: validModels.size(), objectModelThumbnailSrcMap: thumbSrcMap, onlyMine: params.owned == "true"]
    }

    def create() {
        [objectModelInstance: new ObjectModel(params)]
    }

    def upload() {
        def modelFile = request.getFile('objectModel')
        log.debug "Uploading file ${modelFile?.originalFilename}"
        session['filePath'] = fileStorageService.storeUploadedModel(modelFile)
        // This redirect will get eaten up by Dropzone.
        redirect(action: "index")
    }

    def save() {
        params['name'] = params['name'] ?: params['modelNameInput']
        params['modelType'] = params['modelType'] ?: params['modelTypeInput']
        params['imageFilePath'] = null
        params['objFilePath'] = null
        params['bucketName'] = fileStorageService.fileStorageConfiguration.bucketName
        params['status'] = ModelStatus.PENDING
        params['filePath'] = session['filePath']

        def thumb = new Thumbnail()
        if (!thumb.save()) {
            log.warn "Saving thumbnail for ${params.name} failed:"
            thumb.errors.each { log.warn it }
        }

        params['thumbnail'] = thumb

        params['owner'] = userAccountsService.currentUser
        params['permissions'] = userAccountsService.currentUser.preferences.defaultPrivacy

        def objectModelInstance = new ObjectModel(params)
        if (!objectModelInstance.save(flush: true)) {
            log.warn "Saving object model ${params.name} failed:"
            objectModelInstance.errors.each { log.warn it }
            render(view: "create", model: [objectModelInstance: objectModelInstance])
            return
        }

        def renderModelMsg = new ModelRenderMessage()
        renderModelMsg.objectModelId = objectModelInstance.id
        renderModelMsg.bucketName = fileStorageService.fileStorageConfiguration.bucketName
        renderModelMsg.modelKey = session['filePath']
        renderModelMsg.generateObjectFile = true

        // We need to emulate three.js, which sets the angles for the
        // rest of the application.  From the point of view of three.js,
        // the 'side' view is at -90 degrees.  
        renderModelMsg.yaw = -(Math.PI/2.0)

        TextMessage renderModelMsgAsTextMessage = JmsUtil.getMessageAsTextMessage(renderModelMsg)

        log.debug("Sending jms message")
        jmsService.send(service: 'task', method: 'runTask', renderModelMsgAsTextMessage)

        flash.message = message(code: 'default.created.message', args: [
                message(code: 'objectModel.label', default: 'ObjectModel'),
                objectModelInstance.id
        ])
        log.debug("Redirecting to validation screen")
        redirect(action: "validate", id: objectModelInstance.id)
    }

    def validate(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        def modelLocation = fileStorageService.getImageSrcURI(objectModelInstance.objFilePath)
        [objectModelInstance: objectModelInstance, modelLocation: modelLocation]
    }

    def togglePermissions(Long id) {
        def model = ObjectModel.get(id)
        if (userAccountsService.currentUser == model.owner) {
            if (model.permissions == Permissions.PUBLIC) {
                model.permissions = Permissions.PRIVATE
            } else {
                model.permissions = Permissions.PUBLIC
            }
            model.save()
        } else {
            flash.message = message(code: "permissions.toggle.fail", default: "You do not own that model. Cannot toggle permissions")
        }
        redirect([action: "show", id: model.id])

    }

    def show(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "list")
            return
        }
        def modelLocation = fileStorageService.getImageSrcURI(objectModelInstance.objFilePath)
        log.debug("Showing object model with path: ${objectModelInstance.filePath}")
        [objectModelInstance: objectModelInstance, modelLocation: modelLocation]
    }

    def edit(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [objectModelInstance: objectModelInstance]
    }

    def update(Long id, Long version) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (objectModelInstance.version > version) {
                objectModelInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [
                                message(code: 'objectModel.label', default: 'ObjectModel')] as Object[],
                        "Another user has updated this ObjectModel while you were editing")
                render(view: "edit", model: [objectModelInstance: objectModelInstance])
                return
            }
        }

        objectModelInstance.properties = params

        if (!objectModelInstance.save(flush: true)) {
            log.warn "Saving object model ${objectModelInstance.name} failed:"
            objectModelInstance.errors.each { log.warn it }
            render(view: "edit", model: [objectModelInstance: objectModelInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [
                message(code: 'objectModel.label', default: 'ObjectModel'),
                objectModelInstance.id
        ])
        redirect(action: "show", id: objectModelInstance.id)
    }

    def delete(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "list")
            return
        }

        try {
            objectModelInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [
                    message(code: 'objectModel.label', default: 'ObjectModel'),
                    id
            ])
            redirect(action: "show", id: id)
        }
    }

    /**
     * Simply generates the thumbnail for the given image ID
     */
    def generateThumb(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance.thumbnail?.valid) {
            imageService.generateAndStoreThumbnailFromStoredImage(objectModelInstance.thumbnail.id, objectModelInstance.imageFilePath)
        }
    }

    def getFullImageSrc(Long id) {
        log.debug("Generating full image src string")
        def objectModelInstance = ObjectModel.get(id)
        def fullSrc = fileStorageService.getImageSrcURI(objectModelInstance.imageFilePath)
        fullSrc
    }

    /**
     * generates if necessary and returns the thumbnail for the given background id
     * @param id
     * @return
     */
    def thumbImgSrc(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance.thumbnail.valid) {
            log.debug("Thumbnail not valid, telling client to wait")
            render("NOTREADY")
        } else {
            render(imageService.getThumbnailImageSrc(objectModelInstance.thumbnail.id))
        }
    }

    def fullImgDisplayLink(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance.imageFilePath) {
            log.debug("File hasn't shown up yet...")
            render("NOTREADY")
        } else {
            //Link to the display controller
            render(createLink(controller: "imageDisplay", action: "display", params: [filePath: "${objectModelInstance.imageFilePath}"], absolute: true))
        }
    }

    def fullImgDisplaySrc(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (!objectModelInstance.imageFilePath) {
            log.debug("File hasn't shown up yet...")
            render("NOTREADY")
        } else {
            //Link to the display controller
            def fullSrc = fileStorageService.getImageSrcURI(objectModelInstance.imageFilePath)
            render(fullSrc)
        }
    }

    /**
     * Displays the actual thumbnail directly (useful for when it resides on the local filesystem)
     */
    def displayThumb(Long id) {
        generateThumb(id)
        def objectModelInstance = ObjectModel.get(id)

        def thumbnail = imageService.getThumbnailFile(objectModelInstance.thumbnail.id)

        def thumbnailLength = thumbnail.length()
        response.setHeader("Content-length", thumbnailLength.toString())
        response.contentType = 'image/png'
        response.outputStream << thumbnail.getBytes()
        response.outputStream.flush()
        thumbnail.delete()
        return
    }

    def getModelInfo(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        if (objectModelInstance.status == ModelStatus.PENDING) {
            log.debug("Model ${id} pending validation...")
            render("NOTREADY")
        } else {
            def content = g.render(template: "blender", model: [model: objectModelInstance])
            render([content: content, success: (objectModelInstance.status == ModelStatus.SUCCESS)] as JSON)
        }
        if (objectModelInstance.status == ModelStatus.FAILED) {
            objectModelInstance.delete(flush: true)
            log.debug "Deleted failed model ${id}."
        }
    }

    def getObjectFilePath(Long id) {
        def objectModelInstance = ObjectModel.get(id)
        render(objectModelInstance.objFilePath)
    }
}
