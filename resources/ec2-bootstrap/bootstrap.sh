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

#REQUIRED: standalone_apps.tgz
#REQUIRED: sightt.xml

DESTDIR=/opt
#unpack standalone apps
if [ -f "standalone_apps.tgz" ]; then
    tar xf standalone_apps.tgz
    current_dir=`pwd`
    cd ${DESTDIR}
    tar xf ${current_dir}/taskconsumer*.tar
    tar xf ${current_dir}/jobzipper*.tar    
    ln -s taskconsumer*/ taskconsumer
    ln -s jobzipper*/ jobzipper
    cp ${current_dir}/sightt.xml ${DESTDIR}/taskconsumer/resources
else
    echo "ERROR: Could not find standalone_apps.tgz!"
fi


#The files in /tmp/bootstrap are placed there by the bootstrap prep script
#on the bootstrappable AMI
for file in /tmp/bootstrap/*.sh
do
    if [ -f "${file}" ]; then
    #Execute the additional scripts
	echo "Executing ${file}"
	chmod +x ${file}
	${file}
    fi
done

echo "***** BOOTSTRAP COMPLETE *****"
