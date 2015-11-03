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

TOMCAT_HOST=sightt-test
WORKER_HOST=sightt-test
WAR_FILE=sightt.war
TOMCAT_REMOTE_USER=sightt
WORKER_REMOTE_USER=sightt
GRAILS_ENV=sighttdev
WEBAPP_NAME=sightt
HOMEDIR=

. base-upload.sh

do_upload
