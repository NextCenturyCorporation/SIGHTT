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

SSH_CMD=ssh
SCP_CMD=scp

#silent pushd
function spushd {
    pushd $1 > /dev/null
}
#silent popd
function spopd {
    popd $1 > /dev/null
}


SCRIPT_PATH="${BASH_SOURCE[0]}";
if ([ -h "${SCRIPT_PATH}" ]) then
    while([ -h "${SCRIPT_PATH}" ]) do SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
spushd . > /dev/null
cd `dirname ${SCRIPT_PATH}` > /dev/null
SCRIPT_PATH=`pwd`;
spopd  > /dev/null


set -x
#If mysql update is running we cannot stop, because the lock will become stale
MYSQLRUNNING="false"

#Let's actually deal with Ctrl-c
function handler {
    if [ "x$MYSQLRUNNING" == "xfalse" ]; then
	echo "Caught interrupt... Exiting."
	exit 1
    else
	echo "Mysql is still running, cannot interrupt without breaking the DB"
    fi
}

trap handler SIGINT SIGTERM 

function update_standalone {
    VERSION=$(grep version build.gradle | sed 's/.*\"\([^\"]\+\)\".*/\1/')
    BUILD_TAG="THIS PREVENTS SVN UPDATE" ./updater.sh
    ${SCP_CMD} standalone_apps.tgz ${WORKER_REMOTE_USER}@${WORKER_HOST}:
    ${SSH_CMD} ${WORKER_REMOTE_USER}@${WORKER_HOST} -t "tar xf standalone_apps.tgz && tar xf taskconsumer-$VERSION.tar && tar xf jobzipper-$VERSION.tar"
}

function update_web {
    ./grailsw clean
    ./grailsw -Dgrails.env=${GRAILS_ENV} war
    if [ "$(type -t tomcat_preupload)" == "function" ]; then
	tomcat_preupload
    fi
    ${SCP_CMD} target/${WAR_FILE} ${TOMCAT_REMOTE_USER}@${TOMCAT_HOST}:
    ${SSH_CMD} ${TOMCAT_REMOTE_USER}@${TOMCAT_HOST} -t "sudo service tomcat7 stop && sudo rm -rf /var/lib/tomcat7/webapps/${WAR_FILE} /var/lib/tomcat7/webapps/${WEBAPP_NAME}"
    MYSQLRUNNING="true"
    ./grailsw -Dgrails.env=${GRAILS_ENV} dbm-update
    MYSQLRUNNING="false"
    ${SSH_CMD} ${TOMCAT_REMOTE_USER}@${TOMCAT_HOST} -t "sudo cp ${WAR_FILE} /var/lib/tomcat7/webapps/ && sudo service tomcat7 start"
}



function do_upload {
    if [ "$(type -t upload_setup)" == "function" ]; then
	upload_setup
    fi
    spushd ${SCRIPT_PATH}/..
    
    if [ -d "standalone" ]; then
	spushd standalone
	update_standalone
	spopd
    else
	echo "CAN NOT FIND STANDALONE APPS"
	exit 2
    fi
    
    if [ -d "web" ]; then
	spushd web
	update_web
	spopd
    else
	echo "CAN NOT FIND GRAILS APP!"
	exit 1
    fi
    
    spopd
}
