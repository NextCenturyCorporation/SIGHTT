
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
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class BackgroundController {

    def imageService
    def fileStorageService
    def userAccountsService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", togglePermissions: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        Map thumbSrcMap = imageService.getThumbnailSrcList(Background.list(params))
        def backgroundList
        if (params.owned == "true") {
            backgroundList = Background.findAllByOwner(userAccountsService.currentUser)

        } else if (userAccountsService.isAdmin()) {
            backgroundList = Background.list(params)
        } else {
            backgroundList = Background.findAllByOwnerOrPermissions(userAccountsService.currentUser, Permissions.PUBLIC, params)
        }
        [backgroundInstanceList: backgroundList, backgroundInstanceTotal: backgroundList?.size(), backgroundThumbnailSrcMap: thumbSrcMap, onlyMine: params.owned == "true"]
    }

    def create() {
        [backgroundInstance: new Background(params)]
    }

    /**
     *     Save the file, resize if necessary, save the thumbnail
     */
    def save() {

        def multipartfile = request.getFile('backgroundFile')

        // If file is too large, resize it and re-save
        try {
            def tmpFile = fileStorageService.getTempFile()
            multipartfile.transferTo(tmpFile)

            BufferedImage image = ImageIO.read(tmpFile)
            int height = image.getHeight()
            int width = image.getWidth()
            log.warn("  ------------- size of object ${width} ${height} ")

            if (width > 2000 || height > 2000) {
                def largest = width > height ? width : height
                def scale = 2000 / largest
                height = (int) height * scale
                width = (int) width * scale
                image = imageService.getImageScaled(image, width, height)
            }
            fileStorageService.deleteTempFile(tmpFile)

            params['name'] = multipartfile.originalFilename
            params['filePath'] = fileStorageService.saveBackgroundImage(image)
            params['width'] = width
            params['height'] = height
            log.debug "Saved background file ${multipartfile.originalFilename}. Image size:  ${width} x ${height}"

        }
        catch (Exception e) {
            flash.message = message(code: "background.upload.exception", args: ["${multipartfile.originalFilename}", "${e.getMessage()}"]).replace("\n", "\\\n")
            if (params['origin'] != "wizard") {
                render(view: "create")
            } else {
                render("ERROR")
                // redirect(controller: "wizard", action: "selectBackground")
            }
            return
        }

        def thumb = new Thumbnail()
        if (!thumb.save()) {
            log.warn "Saving thumbnail for ${params.name} failed:"
            thumb.errors.each { log.warn it }
        }
        params["thumbnail"] = thumb
        params['owner'] = userAccountsService.currentUser
        params['permissions'] = userAccountsService.currentUser.preferences.defaultPrivacy

        def backgroundInstance = new Background(params)
        if (!backgroundInstance.save(flush: true)) {
            log.warn "Saving background ${params.name} failed:"
            backgroundInstance.errors.each { log.warn it }
            render(view: "create", model: [backgroundInstance: backgroundInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [
                message(code: 'background.label', default: 'Background'),
                backgroundInstance.id
        ])

        if (params['origin'] == "wizard") {
            generateThumb(backgroundInstance.id)
            flash.message = message(code: "wizard.background.uploaded", args: [params['name']])
            flash.selectedBackground = backgroundInstance.id
            log.debug "Set flash.selectedBackground to ${flash.selectedBackground}"
            // This will get eaten up by Dropzone
            redirect(controller: "wizard", action: "selectBackground")
        } else {
            redirect(action: "show", id: backgroundInstance.id)
        }
    }

    def togglePermissions(Long id) {
        log.debug("Toggling permissions")
        def background = Background.get(id)
        log.debug("Background: ${background.name}\nOwned by: ${background.owner}\nBeing toggled by: ${userAccountsService.currentUser.username}")
        if (userAccountsService.currentUser == background.owner) {
            if (background.permissions == Permissions.PUBLIC) {
                background.permissions = Permissions.PRIVATE
            } else {
                background.permissions = Permissions.PUBLIC
            }
            background.save()
        } else {
            flash.message = message(code: "permissions.toggle.fail", default: "You do not own that background. Cannot toggle permissions")
        }
        redirect([action: "show", id: background.id])

    }

    def show(Long id) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [backgroundInstance: backgroundInstance]
    }

    def edit(Long id) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [backgroundInstance: backgroundInstance, fullImageSrc: fileStorageService.getImageSrcURI(backgroundInstance.filePath)]
    }

    def update(Long id, Long version) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (backgroundInstance.version > version) {
                backgroundInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [
                                message(code: 'background.label', default: 'Background')] as Object[],
                        "Another user has updated this Background while you were editing")
                render(view: "edit", model: [backgroundInstance: backgroundInstance])
                return
            }
        }

        backgroundInstance.properties = params

        if (!backgroundInstance.save(flush: true)) {
            log.warn "Saving background ${backgroundInstance.name} failed:"
            backgroundInstance.errors.each { log.warn it }
            render(view: "edit", model: [backgroundInstance: backgroundInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [
                message(code: 'background.label', default: 'Background'),
                backgroundInstance.id
        ])
        redirect(action: "show", id: backgroundInstance.id)
    }

    def delete(Long id) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "list")
            return
        }

        try {
            backgroundInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [
                    message(code: 'background.label', default: 'Background'),
                    id
            ])
            redirect(action: "show", id: id)
        }
    }

    def ajaxLoadBackground(Long id) {
        def background = Background.get(id)
        if (background) {
            render background as JSON
        } else {
            render "ERROR"
        }
    }

    /**
     * Simply generates the thumbnail for the given image ID
     */
    def generateThumb(Long id) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance.thumbnail?.valid) {
            imageService.generateAndStoreThumbnailFromStoredImage(backgroundInstance.thumbnail.id, backgroundInstance.filePath)
        }
    }

    def getFullImageSrc(Long id) {
        log.debug("Generating full image src string")
        def backgroundInstance = Background.get(id)
        def fullSrc = fileStorageService.getFullImageSrc(backgroundInstance.filePath)
        fullSrc
    }

    /**
     * generates if necessary and returns the thumbnail for the given background id
     * @param id
     * @return
     */
    def thumbImgSrc(Long id) {
        def backgroundInstance = Background.get(id)
        if (!backgroundInstance.thumbnail.valid) {
            log.debug("Thumbnail not valid, generating it")
            generateThumb(id)
        }
        render(imageService.getThumbnailImageSrc(backgroundInstance.thumbnail.id))
    }

    /**
     * Displays the actual thumbnail directly (useful for when it resides on the local filesystem)
     */
    def displayThumb(Long id) {
        generateThumb(id)
        def backgroundInstance = Background.get(id)

        def thumbnail = imageService.getThumbnailFile(backgroundInstance.thumbnail.id)

        def thumbnailLength = thumbnail.length()
        response.setHeader("Content-length", thumbnailLength.toString())
        response.contentType = 'image/png'
        response.outputStream << thumbnail.getBytes()
        response.outputStream.flush()
        thumbnail.delete()
        return
    }

}
