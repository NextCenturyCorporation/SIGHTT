
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

import org.springframework.dao.DataIntegrityViolationException

class JobTaskController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [jobTaskInstanceList: JobTask.list(params), jobTaskInstanceTotal: JobTask.count()]
    }

    def create() {
        [jobTaskInstance: new JobTask(params)]
    }

    def save() {
        def jobTaskInstance = new JobTask(params)
        if (!jobTaskInstance.save(flush: true)) {
            log.warn "Saving job task ${jobTaskInstance.id} failed:"
            jobTaskInstance.errors.each { log.warn it }
            render(view: "create", model: [jobTaskInstance: jobTaskInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [
                message(code: 'jobTask.label', default: 'JobTask'),
                jobTaskInstance.id
        ])
        redirect(action: "show", id: jobTaskInstance.id)
    }

    def show(Long id) {
        def jobTaskInstance = JobTask.get(id)
        if (!jobTaskInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [jobTaskInstance: jobTaskInstance]
    }

    def edit(Long id) {
        def jobTaskInstance = JobTask.get(id)
        if (!jobTaskInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [jobTaskInstance: jobTaskInstance]
    }

    def update(Long id, Long version) {
        def jobTaskInstance = JobTask.get(id)
        if (!jobTaskInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (jobTaskInstance.version > version) {
                jobTaskInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [
                                message(code: 'jobTask.label', default: 'JobTask')] as Object[],
                        "Another user has updated this JobTask while you were editing")
                render(view: "edit", model: [jobTaskInstance: jobTaskInstance])
                return
            }
        }

        jobTaskInstance.properties = params

        if (!jobTaskInstance.save(flush: true)) {
            log.warn "Saving job task ${jobTaskInstance.id} failed:"
            jobTaskInstance.errors.each { log.warn it }
            render(view: "edit", model: [jobTaskInstance: jobTaskInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [
                message(code: 'jobTask.label', default: 'JobTask'),
                jobTaskInstance.id
        ])
        redirect(action: "show", id: jobTaskInstance.id)
    }

    def delete(Long id) {
        def jobTaskInstance = JobTask.get(id)
        if (!jobTaskInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "list")
            return
        }

        try {
            jobTaskInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [
                    message(code: 'jobTask.label', default: 'JobTask'),
                    id
            ])
            redirect(action: "show", id: id)
        }
    }
}
