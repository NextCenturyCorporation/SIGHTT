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


function usage {
    cat <<EOF
THIS SCRIPT GENERATES THE BOOTSTRAP PACKAGE FOR EC2
usage: $(basename $0) -d <DIRECTORY> -f [filename]

At a minimum, you must pass a directory to act upon.
At a minimum, that directory must contain:
bootstrap.sh (the main bootstrap script)
  The bootstrap.sh file should have comments near the top of the form
  #REQUIRED: file_required_for_bootstrapping
Additionally, it can contain other files that are dependencies.
Symlinks are allowed.

If you do not provide a filename, it will be called 'bootstrap_standalone.run'
EOF
    
}

function parse-bootstrap-for-reqs {
OLD_IFS=$IFS
IFS=":"
while read -r name value
do
if [ -n "$value" -a "$name" == "#REQUIRED" ]; then
    echo "VALUE: $value"
    REQUIREMENTS+=("${value## }")
fi
done < $BOOTSTRAP_SCRIPT
IFS=$OLD_IFS
}

function package {
if [ ! -d "$DIRECTORY" ]; then
    echo "$DIRECTORY is not a valid directory.  Must provide a directory"
    exit 1
fi
pushd $DIRECTORY
if [ ! -f "$BOOTSTRAP_SCRIPT" ]; then
    echo "$BOOTSTRAP_SCRIPT does not exist in $DIRECTORY"
    exit 2
else
    parse-bootstrap-for-reqs
echo "Requirements: $REQUIREMENTS"
fi

for req in "${REQUIREMENTS[@]}";do
    echo "Checking for $req"
    if [ ! -f "$req" ]; then
	echo "$req does not exist in $DIRECTORY"
	exit 2
    fi
done

WORK_DIR=$(mktemp -d)
echo "Creating temporary work dir: $WORK_DIR"
cat <<EOF
*****************************
Producing a bootstrap package
Directory: $DIRECTORY
Package filename: $FILENAME

main script: $BOOTSTRAP_SCRIPT
app package: $STANDALONE_ARCHIVE
EOF
local DEPS=()
for dep in $(pwd)/*; do
    base=$(basename $dep)
    if [ -f "$base" ]; then
	ln -s ${dep} $WORK_DIR
	if [ "$base" != "$STANDALONE_ARCHIVE" ]; then
	    DEPS+=($(basename $dep))
	fi
    fi
done
if [ -n "$DEPS" ]; then
    echo "dependencies: "
    for dep in ${DEPS[@]}; do
	echo $dep
    done
fi
popd 2>&1 > /dev/null
makeself --follow "$WORK_DIR" "$FILENAME" "$LABEL" "./$BOOTSTRAP_SCRIPT"
echo "Removing temporary work dir: $WORK_DIR"
rm -rf $WORK_DIR
}

BOOTSTRAP_SCRIPT=bootstrap.sh
STANDALONE_ARCHIVE=standalone_apps.tgz
LABEL="EC2 SIGHTT BOOTSTRAP PACKAGE"
FILENAME="bootstrap_standalone.run"
DIRECTORY=
while getopts "d:f:" opt; do
    case $opt in
	d)
	    DIRECTORY=$OPTARG
	    ;;
	f)
	    FILENAME=$OPTARG
	    ;;
    esac
done

if [ -z $DIRECTORY ]; then
    usage
else
    package
fi
