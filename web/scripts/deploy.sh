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

# This was changed to IP when we lost ZoneEdit.  change back when fixed.
# SIGHTT_MAIN=sightt.com
SIGHTT_MAIN=54.235.219.121

GIT_COMMIT=$(git rev-parse --short HEAD) ./grailsw war
scp target/sightt.war ec2-user@${SIGHTT_MAIN}:ROOT.war
ssh -t ec2-user@${SIGHTT_MAIN} "sudo service tomcat7 stop && sudo rm -rf /usr/share/tomcat7/webapps/ROOT* && sudo cp ~ec2-user/ROOT.war /usr/share/tomcat7/webapps/ && sudo service tomcat7 start && sudo tail -f /var/log/tomcat7/catalina.out"
echo "Done"
