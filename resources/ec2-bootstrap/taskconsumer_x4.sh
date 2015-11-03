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


echo "Starting 4 instances of taskconsumer in tmux"
cd /opt
sudo -u ubuntu -H byobu new -d -s taskconsumer -n consumer1 \; \
    send-keys "cd taskconsumer" C-m "bash taskconsumer.sh -c resources/sightt-live.xml" C-m \; \
    neww -n consumer2 \; \
    send-keys "cd taskconsumer" C-m "bash taskconsumer.sh -c resources/sightt-live.xml" C-m \; \
    neww -n consumer3 \; \
    send-keys "cd taskconsumer" C-m "bash taskconsumer.sh -c resources/sightt-live.xml" C-m \; \
    neww -n consumer4 \; \
    send-keys "cd taskconsumer" C-m "bash taskconsumer.sh -c resources/sightt-live.xml" C-m \; \
    selectw -t consumer1
