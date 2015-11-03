#!/bin/bash

# ************************************************************************
#  Copyright (c), 2014 Next Century Corporation. All Rights Reserved.
#
#  This software was developed under the DARPA Visual Media Reasoning
#  project.
#
#                 Next Century Corporation
#             7075 Samuel Morse Drive, Ste 250
#                   Columbia, MD 21046
#                     (443) 545-3100
# ************************************************************************

TOMCAT_HOST=sightt.com
WORKER_HOST=worker1.sightt.com
WAR_FILE=ROOT.war
TOMCAT_REMOTE_USER=ec2-user
WORKER_REMOTE_USER=ubuntu
GRAILS_ENV=production
WEBAPP_NAME=ROOT
HOMEDIR=

. base-upload.sh
function upload_setup {
    SSH_OPTS="-i ${HOME}/.ssh/aws/ec2-sightt-group"
    SSH_CMD="ssh ${SSH_OPTS}"
    SCP_CMD="scp ${SSH_OPTS}"
}

function tomcat_preupload {
    mv target/sightt.war target/${WAR_FILE}
}

do_upload
