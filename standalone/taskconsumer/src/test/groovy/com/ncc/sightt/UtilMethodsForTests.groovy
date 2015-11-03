
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

import groovy.util.logging.*

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

import com.ncc.sightt.s3.S3StorageService

@Log
class UtilMethodsForTests {

    static dummyFile

    static String BLENDER_FILE = "src/test/resources/std.blend"
    static String MULTI_LAYER_BLENDER_FILE = "src/test/resources/multilayer.blend"

    static final RENDER_OUTPUT_FILE = "src/test/resources/office_small.jpg"
    static final BACKGROUND_FILE = "src/test/resources/office_small.jpg"

    static final DEFAULT_COMPOSITED_IMAGE_PATH = "comp/not_a_real_image.png"
    static final DEFAULT_RENDERED_IMAGE_PATH = "render/not_a_real_image.png"
    static final DEFAULT_THUMB_PATH = "thumb/not_a_real_image.png"

    static mockImageIO() {
        ImageIO.metaClass.read = { file ->
            log.info "Reading image: ${file}"
        }
    }

    static unmockImageIO() {
        GroovySystem.metaClassRegistry.removeMetaClass(ImageIO.class)
    }

    static mockS3StorageService() {
        dummyFile = File.createTempFile("dummy", ".tct.tmp")
        S3StorageService.metaClass.loadFileFromStore = { path ->
            def file
            log.info("Loading file '${path}' from store (mocked)")
            file = dummyFile
            file
        }
        S3StorageService.metaClass.saveRenderedFile = { file ->
            log.info "Mocked saving rendered ${file} to ${DEFAULT_RENDERED_IMAGE_PATH}"
            return DEFAULT_RENDERED_IMAGE_PATH
        }
        S3StorageService.metaClass.saveCompositedImage = { comp, base ->
            log.info "Mocked saving composite to ${DEFAULT_COMPOSITED_IMAGE_PATH}, given ${comp} to ${base}..."
            DEFAULT_COMPOSITED_IMAGE_PATH
        }
        S3StorageService.metaClass.saveThumbnailImage = { thumb ->
            log.info "Saved the thumbnail: ${thumb}"
            DEFAULT_THUMB_PATH
        }
    }

    static unmockS3StorageService() {
        GroovySystem.metaClassRegistry.removeMetaClass(S3StorageService.class)
        if (dummyFile.exists()) {
            dummyFile.delete()
        }
    }
}
