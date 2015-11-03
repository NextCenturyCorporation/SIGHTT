
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
// Place your Spring DSL code here


import com.ncc.sightt.ImageService
import com.ncc.sightt.compute.EC2ComputeResourceProvider
import com.ncc.sightt.compute.InstanceConfiguration
import com.ncc.sightt.s3.FileStorageConfiguration
import com.ncc.sightt.s3.LocalFileStorageService
import com.ncc.sightt.s3.S3StorageService
import grails.util.Environment
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.shiro.authc.credential.Sha512CredentialsMatcher
import org.springframework.jms.connection.SingleConnectionFactory

beans = {
    credentialMatcher(Sha512CredentialsMatcher) {
        storedCredentialsHexEncoded = true
        hashSalted = true
        hashIterations = 1024
    }
    instanceConfiguration(InstanceConfiguration) {
        instanceBaseId = "ami-e691ee8f"
        instanceType = "m1.xlarge"
        dataFilePrefix = "ec2-userdata/"
        instanceDataFile = "taskconsumer_x8.json"
        instanceKeyPair = "sightt-group"
        instanceFirewall = "sg-e0d8e688"
        instanceCapacity = 1000
    }

    computeResourceProvider(EC2ComputeResourceProvider) {
        amazonWebService = ref('amazonWebService')
        grailsApplication = ref('grailsApplication')
        instanceConfiguration = ref('instanceConfiguration')
    }

    log.info("Current environment name (in resources): ${Environment.current.name}")
    /*
    Environment.executeForCurrentEnvironment is the PROPER way to do this, but it is currently broken (and has been for the last 18+ months)
    http://jira.grails.org/browse/GRAILS-9101
     */
    switch (Environment.current.name) {
        case ~/^production$/:
            jmsConnectionFactory(SingleConnectionFactory) {
                targetConnectionFactory = { ActiveMQConnectionFactory cf -> brokerURL = 'tcp://ec2-54-243-46-8.compute-1.amazonaws.com:61616' }
            }
            fileStorageConfiguration(FileStorageConfiguration) {
                bucketName = 'sightt-live'
                defaultImageType = 'png'
                backgroundPrefix = 'back/'
                modelPrefix = 'model/'
                objFilePrefix = 'object/'
                thumbnailPrefix = 'thumb/'
                compositedPrefix = 'comp/'
                renderPrefix = 'render/'
            }
            fileStorageService(S3StorageService) { fileStorageConfiguration = ref('fileStorageConfiguration') } 
            break

        case ~/^development$/:
            jmsConnectionFactory(SingleConnectionFactory) {
                targetConnectionFactory = { ActiveMQConnectionFactory cf -> brokerURL = 'tcp://localhost:61616' }
            }
            fileStorageConfiguration(FileStorageConfiguration) {
                bucketName = 'sightt-test'
                defaultImageType = 'png'
                backgroundPrefix = 'back/'
                modelPrefix = 'model/'
                objFilePrefix = 'object/'
                thumbnailPrefix = 'thumb/'
                compositedPrefix = 'comp/'
                renderPrefix = 'render/'
		frontendURL = 'http://localhost:8080/sightt/static/'
            }
            /* fileStorageService(S3StorageService) { fileStorageConfiguration = ref('fileStorageConfiguration') } */
            fileStorageService(LocalFileStorageService) { fileStorageConfiguration = ref('fileStorageConfiguration') }
            imageService(ImageService) { fileStorageService = ref('fileStorageService') }
            break

        case ~/^test$/:
            jmsConnectionFactory(SingleConnectionFactory) {
                targetConnectionFactory = { ActiveMQConnectionFactory cf -> brokerURL = 'vm://localhost' }
            }
            fileStorageConfiguration(FileStorageConfiguration) {
                bucketName = 'sightt-test'
                defaultImageType = 'png'
                backgroundPrefix = 'back/'
                modelPrefix = 'model/'
                objFilePrefix = 'object/'
                thumbnailPrefix = 'thumb/'
                compositedPrefix = 'comp/'
                renderPrefix = 'render/'
            }
            fileStorageService(S3StorageService) { fileStorageConfiguration = ref('fileStorageConfiguration') }
            break
        case ~/^sighttdev$/:
            jmsConnectionFactory(SingleConnectionFactory) {
                targetConnectionFactory = { ActiveMQConnectionFactory cf -> brokerURL = 'tcp://broker:61616' }
            }
            fileStorageConfiguration(FileStorageConfiguration) {
                bucketName = 'sightt-test'
                defaultImageType = 'png'
                backgroundPrefix = 'back/'
                modelPrefix = 'model/'
                objFilePrefix = 'object/'
                thumbnailPrefix = 'thumb/'
                compositedPrefix = 'comp/'
                renderPrefix = 'render/'
            }
            fileStorageService(S3StorageService) { fileStorageConfiguration = ref('fileStorageConfiguration') }
            break

        default:

            break
    }
}
