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

set -x

function cleanup() {
    # echo "Remember to cleanup the tmpdir $TMPDIR"
    exit 0
}

function build() {
    ./gradlew fullBuild
}

trap cleanup SIGINT SIGTERM 

# directory for the script, make it current
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
pushd $SCRIPT_DIR

# If passed in -b, do a build first
if [ "x-b" == "x$1" ]; then
    echo "Building jobzipper"
    ./gradlew fullBuild
else
    echo "Not building jobzipper"
fi

# Get current version from gradle file 
VERSION=$(grep version build.gradle | sed 's/.*\"\([^\"]\+\)\".*/\1/')

# Untar and run
tar xvf $SCRIPT_DIR/jobzipper/build/distributions/jobzipper-$VERSION.tar
pushd jobzipper-$VERSION
bash jobzipper.sh -c sightt-test.xml

# Backout 
popd
popd
cleanup
