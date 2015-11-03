# SIGHTT

SIGHTT is the Synthetic Image Generation Harness for Training and
Testing (SIGHTT).

The purpose of the SIGHTT system is to permit object recognition
researchers to generate a large collection of ground-truthed synthetic
images that span a wide range of operational conditions. Using these
images, researchers and practitioners can both train classifiers
(e.g., object detectors) and evaluate their object recognition
algorithms on very large sets of input data.

# Release Statement

This research was developed with funding from the Defense Advanced
Research Projects Agency (DARPA).

The views expressed are those of the author(s) and do not reflect the
official policy or position of the Department of Defense or the
U.S. Government.

Distribution Statement "A": Approved for Public Release, Distribution Unlimited
 

# Dependencies:

1.  Ubuntu        12.04 LTS or later
1.  activemq  	  5.8 or above  http://activemq.apache.org/
1.  blender   	  blender-2.65a-linux-glibc211-x86_64   http://www.blender.org/download/get-blender/older-versions/
1.  grails*   	  2.0 or above  http://grails.org/   
1.  gradle*   	  1.4 or above  http://www.gradle.org/

Note: Gradle and Grails dependencies are handled through wrapper scripts

## Install ActiveMQ and Blender 

ActiveMQ is installed by untarring the ActiveMQ into a directory.  If desired, add the bin to the path

Blender should be installed in /opt.  A symlink should be made to the actual blender directory.
A dir of /opt should produce something that looks like:

           lrwxrwxrwx  1 root       root                36 Apr 22 13:58 blender -> blender-2.65a-linux-glibc211-x86_64/
           drwxr-xr-x  5 localadmin localadmin        4096 Dec 19  2012 blender-2.65a-linux-glibc211-x86_64
 
SIGHTT looks for, and calls, /opt/blender/blender, where the file blender is the executable.  

# Building and Running Locally 

Here are the instructions for how to build and run SIGHTT on your
local machine; it will connect to S3 as the data storage mechanism.
Hence, you must have an internet connection.

Note, when you run gradlew and/or grailsw it might download and
install a great deal of data, dependencies, and runtimes.  Please let
it continue and finish.  They are designed to do this so that we are
all using the same versions of the software. 

### Build: 

* Run the gradlew script in standalone

        % cd standalone
        % ./updater.sh  (which calls gradlew fullBuild if called without options, but can do a lot more)

### Run:

*  Start an ActiveMQ message queue.  
    *  cd to activemq bin dir 
    *  run activemq with default params:      % ./activemq console      
    *  This will show the console so you can see messages, on port 61616
    *  You can browse to the admin web page at:  http://localhost:8161/ 

    (Default username password is admin/admin, change in activemq/conf/jetty-realm.properties)

* Run task consumer.  In new shell: 
   *  % cd standalone
   *  % ./runner_taskconsumer.sh 

* Run zipper.  In new shell:
   *  % cd standalone
   *  % ./runner_jobzipper.sh

* Build / run web app (on port 8080)
    *  % cd web
    *  % ./grailsw 
    *  Within grails commandline:   run-app


# Using SIGHTT Locally

* In browser, go to:
    * http://localhost:8080/sightt
    * Username:  sightt
    * password:  sightt

* Create a job using the wizard with a single image.  You should see
  it running, first in the task consumer shell window, and then in the
  job zipper shell window.  The Job page should follow along.

* Secret pages (i.e. pages that don't have links)
    * All the object models:   http://localhost:8080/sightt/objectModel/list
    * All the backgrounds:     http://localhost:8080/sightt/background/list

* ActiveMQ:
    * http://localhost:8161/   
    * Username:  admin
    * Password:  admin
    * Take a look at:    http://localhost:8161/admin/queues.jsp
       you should see 1 consumer runTask and 1 in zipTask queues


# Using S3 for Storage

* Change the StorageService in the following files:

          ./standalone/jobzipper/src/dist/resources/sightt-test.xml
          ./standalone/taskconsumer/src/dist/resources/sightt-test.xml
          ./web/grails-app/conf/spring/resources.groovy

  Each of them has a commented out S3StorageService line

* Add AWS credentials to following files:

        .standalone/s3utils/src/main/groovy/com/ncc/sightt/s3/S3StorageService.groovy
        ./web/grails-app/conf/Config.groovy

# Building and Running on SIGHTT.com

* Change the storage service as above to use S3

* Set the database configuration in ./web/grails-app/conf/DataSource.groovy

* Make sure you can connect to EC2 through ssh / scp (see next section)

* Update the standalone (shared libs and tar files)

        % cd standalone
        % ./updater.sh

* cd web and run /scripts/deploy.sh.  This does the following things: 

       * Create the war 

               % cd web
               % grailsw
                    grails> -Dgrails.env=production war
        	        grails> exit

      * Copy war to sightt.com
      
              % scp target/sightt.war ec2-user@sightt.com:ROOT.war

      * Go to sightt.com and restart tomcat

                % ssh ec2-user@sightt.com
                % sudo service tomcat7 stop
                % sudo rm -rf /var/lib/tomcat7/webapps/ROOT* 
                % sudo cp ROOT.war /var/lib/tomcat7/webapps/ 
                % sudo service tomcat7 start
                % sudo tail -f /var/log/tomcat7/catalina.out
    
          (The last command will just show you the output, so you can see if there are errors).

* Copy new standalone to jobzipper and restart

        % scp standalone_apps.tgz ubuntu@54.225.72.232:   
        % ssh ubuntu@54.225.72.232
        % byobu
        % Cntl-c the currently running job zipper
        % rm -rf jobzipper-0.0.7-SNAPSHOT*
        % rm -rf taskconsumer-0.0.7-SNAPSHOT*
        % tar xvfz standalone_apps.tgz
        % tar xvf jobzipper-0.0.7-SNAPSHOT.tar 
        % cd jobzipper-0.0.7-SNAPSHOT/
        % bash jobzipper.sh -c resources/sightt-live.xml 
        % <f6>                                             (safely get out of byobu)

* Re-create bootstrap file and tell worker to re-bootstrap

        % cd sightt/resources/ec2-bootstrap
        % mkdir test
        % cd test
        % ln -s ../../../standalone/standalone_apps.tgz .
        % ln -s ../bootstrap.sh .
        % cp <someplace>/sightt.xml .
        % modify sightt.xml
        % cd ..
        % make-bootstrap.sh -d test -f bootstrap_allparts.run
        % [copy bootstrap_allparts.run to s3/sightt-bootstraps/
        % cd ../../
        % ./bootstrap_worker.sh ubuntu@ec2-184-73-20-77.compute-1.amazonaws.com

