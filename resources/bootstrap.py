#!/usr/bin/env python

# This script starts the process to bootstrap an EC2 instance. It is
# designed to abstract instance specific bootstrapping logic, and so
# retrieves the instance specific bootstrapping script via S3, given
# appropriate instance user data described below.
#
# Instance user data needs to provide:
#  - an S3 bucket name to retrieve a bootstrapping script from
#  - an S3 access key and secret key as environment variables accessible to
#    the user running the script, i.e.
#      AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
#  - a script by filename to retrieve from S3 and run
#
# User data is expected to be JSON of the form:
#
"""
{
    "bootstrap": {
        "bucket_name": "vca-bootstrap",
        "script": {
            "file": "bootstrap-cs.py",
            "args": {
                "type": "del"
            }
        }
    },

    "dependencies": [
        {
        "bucket_name": "bucket",
        "path": "path"
        },
        {
        "bucket_name": "bucket",
        "path": "path2"
        }]
}
"""
#
# The highlevel "bootstrap" namespace should contain all bootstrapping
# configuration information. This data format can be extended by adding
# other high level namespaces such as the "other" example above.
#
# The minimal config is a bootstrap key, with associated script and
# script file data, and a bucket_name. Optional arguments can be passed
# to the bootstrapping script by specifying an args key and values (this
# is currently unimplemented).
#
# Any other script calling this script should check for standard POSIX
# return code conventions to determine success or failure.
#


import os
import os.path
import sys
import stat
import subprocess

import boto
import boto.utils
from boto.s3.key import Key

import simplejson as json


def main():
    print "***** BOOTSTRAPPING WORKER *****"
    try:
        os.mkdir('/tmp/bootstrap')
    except Exception, e:
        print >>sys.stderr,	"Exception:",e

    try:
        user_data = boto.utils.get_instance_userdata()
    except AWSConnectionError, e:
        print >>sys.stderr, "Couldn't connect to AWS to retrieve user data:", e
        return 1

    # parse our userdata
    try:
        json_data = json.loads(user_data)
    except json.decoder.JSONDecodeError, e:
        print >>sys.stderr, "Couldn't parse JSON data:", e
        return 1

    # check that we have enough configuration to attempt bootstrap
    if 'bootstrap' in user_data and 'script' in json_data['bootstrap']:
        json_data = json.loads(user_data)
        bootstrap_config = json_data['bootstrap']
        script_config = bootstrap_config['script']
    else:
        print "Required bootstrap info not available for this instance. Missing bootstrap or script config."
        return 0

    if 'bucket_name' in bootstrap_config and 'file' in script_config:
        bucket_name = bootstrap_config['bucket_name']
        script_name = bootstrap_config['script']['file']
    else:
        print "Required bootstrap info not available for this instance. Missing bucket name or script file."
        return 0

    if 'args' in bootstrap_config['script']:
        script_args = bootstrap_config['script']['args']

    # build a local path which we'll save our bootstrapping script to
    local_script = os.path.normpath(os.path.join('/tmp/bootstrap',script_name))

    # connect to s3 and retrieve our bootstrapping script - connect_s3 uses 
    # AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY env vars if not passed explicitly
    try:
        s3_conn = boto.connect_s3()
        bucket = s3_conn.get_bucket(bucket_name)
    except Exception, e:
        print >>sys.stderr, "Exception:", e
        return 1
    else:
        k = Key(bucket)
        k.key = script_name
        k.get_contents_to_filename(local_script)

    # chmod it and run the darn thing (perm: 700)
    script_perm = stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR
    os.chmod(local_script, script_perm)

    # Grab the dependencies
    if 'dependencies' in json_data:
        for dep in json_data['dependencies']:
            try:
                s3_conn = boto.connect_s3()
                bucket = s3_conn.get_bucket(bucket_name)
            except Exception, e:
                print >>sys.stderr, "Exception:", e
                return 1
            else:
                k = Key(bucket)
                k.key = dep['path']
                local_dep = os.path.normpath(os.path.join('/tmp/bootstrap',k.key))
                k.get_contents_to_filename(local_dep)
                os.chmod(local_dep,stat.S_IRUSR | stat.S_IWUSR)
    
    if os.path.exists(local_script):
        try:
            # TODO: implement script args if provided
            retcode = subprocess.call(local_script)
            if retcode != 0:
                print >>sys.stderr, "Script returned non-zero exit code:" + local_script
                raise OSError(retcode, "Script returned non-zero exit code: " + local_script)
        except OSError, e:
            print >>sys.stderr, "Execution failed:", e
            return 1


if __name__ == '__main__':
    try:
        with open('/BOOTSTRAP'): pass
    except IOError:
	print "System already bootstrapped, skipping"
	sys.exit(0)
        # Remove the bootstrap trigger and continue bootstrapping                                                                                     
    try:
        os.remove('/BOOTSTRAP')
    except Exception, e:
	print >>sys.stderr, "Couldn't remove bootstrap trigger file...", e
    sys.exit(main())
