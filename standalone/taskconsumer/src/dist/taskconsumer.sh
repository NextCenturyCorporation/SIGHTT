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

# Start the task consumer
java -classpath resources/:lib/*:. com.ncc.sightt.TaskConsumerRunner $*

