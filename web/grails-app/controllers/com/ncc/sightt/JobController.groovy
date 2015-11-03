
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
import com.ncc.sightt.JobStatus
import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

class JobController {

    def jmsService
    def taskService
    def fileStorageService
    def imageService
    def userAccountsService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST",
            cloneJob: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def joblist
        if (userAccountsService.isAdmin()) {
            joblist = Job.list(params)
        } else {
            joblist = Job.findAllByOwnerOrPermissions(userAccountsService.currentUser, Permissions.PUBLIC, params)
        }
        [jobInstanceList: joblist, jobInstanceTotal: Job.count()]
    }

    def create() {
        log.debug("Create:  ${params}")
        def jobInstance = new Job(params.job)
        jobInstance.config = new JobConfig(params.config)
        [jobInstance: jobInstance]
    }

    def superSecretForceDelete(Long id) {
        log.debug("Using the super secret force delete method on job ${id}!!!")
        [id: id]
    }

    def save() {
        log.debug("Job:  ${params.job}")
        log.debug("Job Config:  ${params.config}")

        def jobMap = params['job']
        def jobConfigMap = params['config']

        jobMap += ['owner': userAccountsService.currentUser]
        if (jobMap.permissions == null) {
            jobMap += ['permissions': userAccountsService.currentUser.preferences.defaultPrivacy]
        }
        jobMap += ['submitDate': new Date()]
        jobMap += ['status': JobStatus.CREATED]
        jobMap += ['numTasks': 0]
        jobMap += ['numComplete': 0]
        def jobConfig = new JobConfig(jobConfigMap)
        jobMap += ['config': jobConfig]

        log.debug("job config after creation with map: " + jobConfig)

        // Experimental end time estimate:  30 seconds + 5 * |results|
        def estimate = 30 + (5 * (jobConfigMap['numImages'].toInteger()))
        def calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, estimate)
        def estimatedEndDate = calendar.getTime()
        jobMap += ['expectedEndDate': estimatedEndDate]

        def jobInstance = new Job(jobMap)

        if (!jobInstance.save(flush: true)) {
            log.warn "Saving job ${jobInstance.id} failed:"
            jobInstance.errors.each { log.warn it }
            render(view: "create", model: [jobInstance: jobInstance])
            return
        }

        // Send message for TaskService to create / run all the tasks
        jmsService.send(service: 'task', method: 'generateAndRunTasks', jobInstance.id)

        flash.message = message(code: 'default.created.message', args: [
                message(code: 'job.label', default: 'Job'),
                jobInstance.id
        ])

        redirect(action: "show", id: jobInstance.id)
    }

    def show(Long id) {
        log.debug("Showing job ${id}")
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }

        def images = null
        def jobPreviewList = null
        def hasErrors = false

        if (JobTask) {
            def completedJobs = JobTask.where {
                (job == jobInstance && completed == true && error == "")
            }
            def completedJobList = completedJobs.list()
            Collections.shuffle(completedJobList)
            jobPreviewList = completedJobList.subList(0, Math.min(4, completedJobList.size())).sort { it.id }
            images = jobPreviewList*.compositeImage

            def failedJobs = JobTask.where {
                (job == jobInstance && completed == true && error != "")
            }
            hasErrors = failedJobs.list().size() > 0
        }

        def thumbMap = imageService.getThumbnailSrcList(images)

        [
                jobInstance: jobInstance,
                compositeImageThumbnailSrcMap: thumbMap,
                jobPreviewList: jobPreviewList,
                jobHasErrors: hasErrors
        ]
    }

    def edit(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }

        [jobInstance: jobInstance]
    }

    def update(Long id, Long version) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (jobInstance.version > version) {
                jobInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [
                                message(code: 'job.label', default: 'Job')] as Object[],
                        "Another user has updated this Job while you were editing")
                render(view: "edit", model: [jobInstance: jobInstance])
                return
            }
        }

        jobInstance.properties = params

        if (!jobInstance.save(flush: true)) {
            log.warn "Saving job ${jobInstance.id} failed:"
            jobInstance.errors.each { log.warn it }
            render(view: "edit", model: [jobInstance: jobInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [
                message(code: 'job.label', default: 'Job'),
                jobInstance.id
        ])
        redirect(action: "show", id: jobInstance.id)
    }

    def delete(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }

        try {
            jobInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "show", id: id)
        }
    }

    def cloneJob(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }
        flash.jobInstance = jobInstance
        forward(controller: "wizard", action: "cloneJob")
    }

    def errors(Long id) {
        def jobInstance = Job.get(id)
        if (!jobInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                    message(code: 'job.label', default: 'Job'),
                    id
            ])
            redirect(action: "list")
            return
        }

        def failedJobList = null
        if (JobTask) {
            def failedJobs = JobTask.where {
                (job == jobInstance && completed == true && error != "")
            }
            failedJobList = failedJobs.list()
        }

        [jobId: id, failedJobList: failedJobList]
    }

    /**
     * Called by ajax to return the updated task progress meter
     * @param id of the task
     * @return the html for the updated task progress meter
     */
    def updateJobProgress(Long id) {
        def theJob = Job.get(id)
        //log.debug("ID ${id}, job: ${theJob as JSON}")
        //assert(theJob)

        render([html: html, complete: theJob.isComplete] as JSON)
    }

    def updateTask(Long id) {
        log.debug("Updating job task ${id}")
        def task = JobTask.get(id)
        def theJob = Job.get(task.job.id)
        assert (task)
        assert (theJob)

        def jobPreviewList
        def images
        if (JobTask) {
            def query = JobTask.where { (job == theJob && completed == true) }
            def allCompleted = query.list()
            Collections.shuffle(allCompleted)
            jobPreviewList = allCompleted.subList(0, Math.min(4, allCompleted.size())).sort { it.id }
            images = jobPreviewList*.compositeImage
        } else {
            jobPreviewList = null
            images = null
        }
        def thumbSrcList = imageService.getThumbnailSrcList(images)
        def taskHtml = g.render(template: "tasklist", model: [it: task, compositeImageThumbnailSrcMap: thumbSrcList])
        def progHtml = g.render(template: "progress", model: [jobInstance: theJob])
        render([html: taskHtml, progress: progHtml] as JSON)
    }

    def updateProgress(Long id) {
        def job = Job.get(id)
        def progHtml = g.render(template: "progress", model: [jobInstance: job])
        render([progress: progHtml] as JSON)
    }

    /**
     * Return the results as a zip file.  Gather the files from the FileStorageService
     * @param id
     * @return
     */
    def getZipFile(Long id) {
        def jobInstance = Job.get(id)
        redirect(url: fileStorageService.getImageSrcURI(jobInstance.zipFilePath))
    }
}
