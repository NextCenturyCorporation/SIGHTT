
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
package com.ncc.sightt.s3

import org.junit.After

import static org.junit.Assert.*
import groovy.util.logging.Log4j

import org.junit.Before
import org.junit.Test


@Log4j
class ModelBackgroundCacheTest {
    static final EXISTING_BACKGROUND = "existingBackground"
    static final NON_EXISTING_BACKGROUND = "nonExistingBackground"
    static final EXISTING_MODEL = "existingModel"
    static final NON_EXISTING_MODEL = "nonExistingModel"

    def mbc

    void mockS3StorageService() {
        log.info "Mocking S3StorageService"
        LocalFileStorageService.metaClass.retrieveObjectFromStoreAsStream = { String filePath ->
            log.info "Grabbing ${filePath} "
            InputStream bytes = new ByteArrayInputStream("1".getBytes())
            bytes
        }

        LocalFileStorageService.metaClass.saveFileToStore = { String prefix, File file, String suffix ->
            log.info("Uploading file to store")
            "randomFileKey"
        }

        LocalFileStorageService.metaClass.loadFileFromStore = { String filePath ->
            new File()
        }
    }

    void unmockS3StorageService() {
        GroovySystem.metaClassRegistry.removeMetaClass(LocalFileStorageService.class)
    }

    @Before
    void setUp() {
        mockS3StorageService()

//        UtilMethodsForS3Tests.mockS3StorageService()
        mbc = new ModelBackgroundCache()
        mbc.storageService = new LocalFileStorageService()
//        UtilMethodsForS3Tests.unmockS3StorageService()
    }

    @After
    void tearDown() {
        unmockS3StorageService()
    }

    @Test
    void testConstructor() {
        assert mbc.modelCache == [:]
        assert mbc.backgroundCache == [:]
        assert mbc.storageService
    }

//    @Test
    void testGetNonExistingEntry() {
        def cacheResult = mbc.getModel(NON_EXISTING_MODEL)
        assert !cacheResult.cached
        assert cacheResult.result instanceof File

        cacheResult = mbc.getBackground(NON_EXISTING_BACKGROUND)
        assert !cacheResult.cached
        assert cacheResult.result instanceof File
    }

//    @Test
    void testGetExistingEntry() {
        def cacheResult = mbc.getModel(EXISTING_MODEL)
        assert !cacheResult.cached
        cacheResult.result.createNewFile()
        cacheResult = mbc.getModel(EXISTING_MODEL)
        assert cacheResult.cached
        assert cacheResult.result instanceof File
        cacheResult.result.delete()

        cacheResult = mbc.getBackground(EXISTING_BACKGROUND)
        assert !cacheResult.cached
        cacheResult.result.createNewFile()
        cacheResult = mbc.getBackground(EXISTING_BACKGROUND)
        assert cacheResult.cached
        assert cacheResult.result instanceof File
        cacheResult.result.delete()
    }
}
