
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

import groovy.util.logging.Log4j

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Main class for the Task Consumer.  All it does is check the passed parameters
 * and load up the spring xml file that does all the work.
 */
@Log4j
class TaskConsumerRunner {

    def config = 'sightt-live.xml'
    def static configArray = []

    static main(args) {

        TaskConsumerRunner r = new TaskConsumerRunner()
        r.parseArgs(args)
        r.run()
    }

    def parseArgs(args) {
        def cli = new CliBuilder(usage: 'TaskConsumerRunner [options] ')
        cli.h(longOpt: 'help', 'Show usage information')
        cli.c(argName: 'config', longOpt: 'config', args: 1, 'Configuration file to use, defaults to ' + config)

        def options = cli.parse(args)
        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        if (options.c) {
            config = options.c
            configArray.add(config)
            log.warn("Using configuration file: ${config}")
        }
    }

    def run() {
        def ctx = new ClassPathXmlApplicationContext((String[]) configArray.toArray())
    }
}
