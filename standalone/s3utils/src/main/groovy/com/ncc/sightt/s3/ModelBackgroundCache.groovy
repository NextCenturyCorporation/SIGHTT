
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

import groovy.util.logging.Log4j

@Log4j
class ModelBackgroundCache {
    def backgroundCache
    def modelCache
    def storageService



    ModelBackgroundCache() {
        backgroundCache = [:]
        modelCache = [:]
//        storageService = new S3StorageService()
//        storageService.fileStorageConfiguration = fileStorageConfiguration
    }

    def getCacheForType(entryType) {
        def cache
        switch (entryType) {
            case ModelEntryType.MODEL_ENTRY:
                cache = modelCache
                break
            case ModelEntryType.BACKGROUND_ENTRY:
                cache = backgroundCache
                break
            default:
                log.error("Unknown cache requested!")
                break
        }
        log.debug("CACHE: ${cache}")
        cache
    }

    def performCacheLookup(cache, key) {
        def entryWasInCache = false
        def tmpResult = cache[key]
        def result

        if (tmpResult) {
            //entry was in cache
            log.debug("Cache Hit: ${key}")
            entryWasInCache = true
            result = new File(tmpResult)
            if (!result.exists()) {
                log.error("Cache Corrupted: ${tmpResult} does not exist!")

                entryWasInCache = false
            }
        }
        if (!entryWasInCache) {
            //Cache miss
            log.debug("Cache Miss: ${key}")
            entryWasInCache = false
            result = storageService.loadFileFromStore(key)
            log.debug("Storing ${key} at ${result.path}")
            cache[key] = result.path
        }

        [cached: entryWasInCache, result: result]
    }


    def getEntryFromCache(entryType, key) {
        def cache
        def result
        cache = getCacheForType(entryType)
        result = performCacheLookup(cache, key)
        result
    }

    void clearCache() {
        log.info("Clearing backgroundCache")
        for (entry in backgroundCache) {
            def tmpFile = new File(entry)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
        log.info("Clearing modelCache")
        for (entry in modelCache) {
            def tmpFile = new File(entry)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
    }

    def getBackground(key) {
        getEntryFromCache(ModelEntryType.BACKGROUND_ENTRY, key)
    }

    def getModel(key) {
        getEntryFromCache(ModelEntryType.MODEL_ENTRY, key)
    }
}
