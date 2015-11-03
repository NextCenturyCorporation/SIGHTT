
The files in this directory were generated using xjc from JAXB, part of the JDK, in particular from 
/usr/lib/jvm/java-1.6.0-openjdk-amd64/bin/xjc on Ubuntu 12.04 LTS.


Note: Object was changed so that the element ClassID is a String vice
its own object type, similar to ObjectID.  ClassID was omitted from
the DTD.  I think that it should bo after objectID in the following: 

    <!ELEMENT name (#PCDATA)>
    <!ELEMENT objectID (#PCDATA)>
    <!ELEMENT occlusion (#PCDATA)>
    <!ELEMENT polygon (pt)+>

Here's what I think it should be: 

    <!ELEMENT name (#PCDATA)>
    <!ELEMENT objectID (#PCDATA)>
    <!ELEMENT classID (#PCDATA)>       <-- Insertion here
    <!ELEMENT occlusion (#PCDATA)>
    <!ELEMENT polygon (pt)+>

and that is what the code has been modified to use.  


% xjc -dtd -d src -p com.ncc.sightt.metadata MetaDataFormatXml.dtd
parsing a schema...
compiling a schema...
com/ncc/sightt/metadata/Annotation.java
com/ncc/sightt/metadata/ClassID.java
com/ncc/sightt/metadata/DistanceFromCamera.java
com/ncc/sightt/metadata/Object.java
com/ncc/sightt/metadata/ObjectFactory.java
com/ncc/sightt/metadata/ObjectParts.java
com/ncc/sightt/metadata/Points.java
com/ncc/sightt/metadata/Polygon.java
com/ncc/sightt/metadata/Pt.java
com/ncc/sightt/metadata/Size.java
com/ncc/sightt/metadata/Source.java
com/ncc/sightt/metadata/ViewAngle.java

And then copied them into src/java.  

