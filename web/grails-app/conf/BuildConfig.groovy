
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
grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/${appName}.war"
grails.tomcat.nio = true

forkConfig = [maxMemory: 2048, minMemory: 64, debug: false, maxPerm: 512]
grails.project.fork = [
        test: false, // configure settings for the test-app JVM
        run: forkConfig, // configure settings for the run-app JVM
        war: forkConfig, // configure settings for the run-war JVM
        console: forkConfig // configure settings for the Swing console JVM
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }

    // ---------------------------------------------------------------------------
    // Modify the following to see the includes / dependencies when starting up Grails.
    log "info" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'


    checksums true // Whether to verify checksums on resolve
    legacyResolve false
    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()

        grailsCentral()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "https://oss.sonatype.org/content/repositories/snapshots/"
        mavenRepo "https://oss.sonatype.org/content/groups/public"
	mavenRepo "http://download.osgeo.org/webdav/geotools/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime 'mysql:mysql-connector-java:5.1.20'
        //adding cobertura here is a workaround from:http://jira.grails.org/browse/GPCODECOVERAGE-50
        ////test 'net.sourceforge.cobertura:cobertura:1.9.4.1'

        // NOTE:  AWS is being moved to S3Utils, and might require these.
        //
        //    //AWS follows
        //    // Workaround to resolve dependency issue with aws-java-sdk and http-builder (dependent on httpcore:4.0)
        build 'org.apache.httpcomponents:httpcore:4.2'
        build 'org.apache.httpcomponents:httpclient:4.2'
        runtime 'org.apache.httpcomponents:httpcore:4.2'
        runtime 'org.apache.httpcomponents:httpclient:4.2'

        compile 'org.atmosphere:atmosphere-runtime:1.1.0.RC1'
        compile 'com.ncc.sightt:jmslib:0.0.9-SNAPSHOT'
        compile 'com.ncc.sightt:s3utils:0.0.9-SNAPSHOT'
        compile 'java3d:vecmath:1.5.2'
    }

    plugins {
        build ":tomcat:7.0.52.1"

        compile ":jquery-ui:1.10.3"
        compile ":platform-core:1.0.M6.1"
        compile ":shiro:1.2.0", { excludes 'commons-logging' }
        compile ':cache:1.0.1'
        compile ":executor:0.3"
        compile ":build-info:1.2.3"
        compile ":jms:1.3-SNAPSHOT"
        compile ":mail:1.0.1"
        compile ":recaptcha:0.6.2"

        runtime ":hibernate:3.6.10.10"
        runtime ":jquery:1.10.2.2"
        runtime ":database-migration:1.4.0"
	runtime ":file-server:0.2.1"
        runtime ":resources:1.2.7"
        runtime ":aws-sdk:1.7.7"

        test ":code-coverage:1.2.7"
    }
}

coverage {
    //Don't perform code coverage analysis for generated classes that we have no control over.
    exclusions = ["**/conf/**", "**/gsp_*/**", "**/changelog*", "**/changelog\$_*/**"]
    enabledByDefault = false
}
/*
grails.tomcat.jvmArgs = [
        "-server",
        "-Xms2048m",
        "-Xmx2048m",
        "-XX:+UseConcMarkSweepGC",
        "-XX:+CMSClassUnloadingEnabled",
        "-XX:+CMSIncrementalMode",
        "-XX:-UseGCOverheadLimit",
        "-XX:+ExplicitGCInvokesConcurrent"
]*/
