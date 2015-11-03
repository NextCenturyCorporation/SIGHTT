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

DIALOG=${DIALOG=dialog}

function clean_database {
    host=$1
    dbname=$2
    user=$3
    password=$4

    mysql -u$user -p$password --host $1 <<EOF
drop database $dbname;
create database $dbname;
EOF
}


clean_database sightt-test sightt_test root