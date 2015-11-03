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

VERSION=
ARCHIVE_TYPE=tar
## All arguments are passed to gradlew (for instance --stacktrace and --debug)

GRADLE_SUCCESS=

#Let's actually deal with Ctrl-c
function handler {
    echo "Caught interrupt... Exiting."
    exit 1
}

trap handler SIGINT SIGTERM 

#This gets annoying...
if [ "x$1" == "xdebug" ]; then
    set -x
    shift
fi

#silent pushd
function spushd {
    pushd $1 > /dev/null
}
#silent popd
function spopd {
    popd $1 > /dev/null
}

function recover_if_needed {
    if [ "$GRADLE_SUCCESS" -ne "0" ]; then
	echo "Gradle build failed, repairing projects that may be broken..."
	#./gradlew -q eclipse
    fi
}


function build_any {
    proj=$1
    shift
    args=$*
    echo "Performing full build on $proj"
    spushd $proj
    spopd
    ./gradlew clean :$proj:fullBuild $args
    GRADLE_SUCCESS=$?
    recover_if_needed
}

function fullBuild {
    if [ -z "$BUILD_TAG" ]; then
	echo "Building GIT revision: $(git rev-parse HEAD)"
    fi
    ./gradlew clean fullBuild $*
    GRADLE_SUCCESS=$?
    #added in case the build or tests fail
    recover_if_needed

}

function package {
    echo "Packaging"
    tar --transform 's|\(.*/\)\([^/]\+\)$|\2|' -czvf standalone_apps.tgz jobzipper/build/distributions/jobzipper-$VERSION.$ARCHIVE_TYPE taskconsumer/build/distributions/taskconsumer-$VERSION.$ARCHIVE_TYPE
}

SCRIPT_PATH="${BASH_SOURCE[0]}";
if ([ -h "${SCRIPT_PATH}" ]) then
    while([ -h "${SCRIPT_PATH}" ]) do SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
spushd . > /dev/null
cd `dirname ${SCRIPT_PATH}` > /dev/null
SCRIPT_PATH=`pwd`;
spopd  > /dev/null

spushd $SCRIPT_PATH

##
## Actual code starts here
##

#get the version number:
VERSION=$(grep version build.gradle | sed 's/.*\"\([^\"]\+\)\".*/\1/')
orig_args=$*
proj=$1
shift
args=$*

spushd $SCRIPT_PATH

case $proj in
    jmsLib)
	build_any $proj $args
	;;
    
    s3utils)
	build_any $proj $args
	;;
    
    jobzipper)
	build_any $proj $args
	;;
    
    taskconsumer)
	build_any $proj $args
	;;
    package)
	package
	;;
    **)
	echo "Updating all standalone projects..."
	fullBuild $orig_args
	if [ "$GRADLE_SUCCESS" -eq "0" ]; then
	    package
	else
	    echo "Gradle failed to build, skipping packaging"
	    exit 1
	fi
	;;
esac

echo "Leaving standalone projects"
spopd
