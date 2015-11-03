
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
package com.ncc.sightt.geo

import com.ncc.sightt.Background
import org.codehaus.groovy.grails.web.json.JSONObject

class DefineGeometryController {

    def geometryJsonService
    def fileStorageService

    /**
     * Allows the user to draw a geometry on a given background.
     * @return
     */
    def drawing() {
        Background background = Background.get(params.id)
        JSONObject geometryJson = null
        if (background?.geometry) {
            geometryJson = new JSONObject(background.geometry.iterator().next().json)
        }

        def imageUrl = fileStorageService.getImageSrcURI(background?.filePath)
        [id: params.id, backgroundUrl: imageUrl, geometryJson: geometryJson]
    }

    /**
     * Saves the geometry on the screen.
     * Currently only one geometry may be saved.
     * @return
     */
    def saveGeometry() {
        Background background = Background.get(params.id)
        Geometry geometry = geometryJsonService.createGeometry(request.JSON)
        removeExistingGeometry(background)

        background.addToGeometry(geometry)
        if (!background.save()) {
            log.warn "Saving background ${background.name} failed:"
            background.errors.each { log.warn it }
            render(contentType: "text/json") { success = 'false' }
        }
        render(contentType: "text/json") { success = 'true' }
    }

    private void removeExistingGeometry(Background background) {
        if (background.geometry) {
            background.geometry.each {
                it.delete()
                background.geometry.remove(it)
            }
        }
    }

    def deleteGeometry() {
        Background background = Background.get(params.id)
        removeExistingGeometry(background)
        redirect(controller: "background", action: "list")
    }
}
