<html>
<head>
    <meta name="layout" content="main">
    <title>SIGHTT Frequently Asked Questions</title>
    <r:require module="faq"/>
    <r:script>
        $(document).ready(function () {
            dynamicFaq();
        });
    </r:script>
</head>

<body id="faq">
<div id="container">

<div id="title" class="container chapter3">
    <h1 class="faq">Frequently Asked Questions about SIGHTT</h1>
</div>

<div id="content">
<div class="container">
    <h2>General</h2>
    <dl>
        <dt>&rsaquo; What is SIGHTT?</dt>
        <dd>
            <p>SIGHTT stands for "Synthetic Image Generation Harness for
            Training and Testing"</p>

            <p>One of the issues facing image recognition researchers is
            the generation and maintenance of ground-truthed images. If you
            are developing an algorithm to find bicycles, then you need many
            images with bicycles (and many without) on which to train and test
            your algorithm. Creating those images is expensive and
            time-consuming. SIGHTT is intended to allow the researcher to
            specify what object they want in the image and what the background
            should be, and SIGHTT will create composite images, with labeled
            metadata saying where the object is in the composite.</p>
        </dd>

        <dt>&rsaquo; Something went wrong. How do I ask for help?</dt>
        <dd>
            <p>Email to info@sightt.com and we will try to help you.</p>
        </dd>

    </dl>
</div>

<div class="container">
    <h2>Backgrounds</h2>

    <dl>
        <dt>&rsaquo; How do I upload a background?</dt>
        <dd>
            <p>From the home page, click on the wizard. The first page
            allows you to upload a background image. Click on browse (or
            double-click in the text area) and navigate to the image you want
            to download.  Or, drag-and-drop the image into the center of the page.</p>

            <p>Please make sure that you have the rights to the image that
            you upload. The image will currently be visible to all users.</p>
        </dd>
        <dt>&rsaquo; Is there a maximum size for a background?</dt>
        <dd>
            <p>Backgrounds are currently limited to 2000x2000 pixels. This
            restriction may change in the future</p>
        </dd>
    </dl>

</div>

<div class="container">
    <h2>Models</h2>

    <dl>
        <dt>&rsaquo; How do I upload a model?</dt>
        <dd>
            <p>From the home page, click on the 'Upload' link, navigate to
            your model, and then click upload. SIGHTT will render the model and
            take you to either a screen where you can see the rendered model or
            a page that will explain what went wrong.</p>

            <p>Please make sure that you have the rights to the model that
            you upload. The model will currently be visible to all users.</p>
        </dd>


        <dt>&rsaquo; What are the requirements for a model?</dt>
        <dd>
            <p>We are using Blender (version 2.65) to render the model and
            composite it into the background. We use a python script to rotate
            the object. Therefore we have the following requirements for the
            model:</p>

            <ul>
                <li>It can be rendered with a 'render' command. If you click
                on F12 in Blender and it creates an image, the rendering should
                work.</li>
                <li>Can be treated as a single object. The rotation command
                assumes that the currently selected object in the Blender file
                consists of a single object, so that the rotate command (see
                below) can rotate it as a whole. To select the object in Blender,
                open Blender, open a 'Outliner' view, and click on the object (the
                title will turn white), save the Blender file. If your object
                consist of multiple scene elements, then you will need to group
                them into a single object.</li>
                <li>Can be rotated with a python command that looks like:
                "bpy.ops.transform.rotate(value=(0.1), axis=(-1, 0, 0))". Try
                putting this command into a Blender Console window. It should
                rotate your model.</li>
                <li>Is lighted. SIGHTT will eventually have lighting based on
                the background, but for now, the model needs to have lighting as
                part of the .blend file.</li>
                <li>Has a camera in the XY plane, pointing at the object with
                a field of view that will encompass the entire object throughout
                rotations. SIGHTT rotates the object, calls render, and then
                composites. If, at some rotations, the object goes out of frame,
                it will be chopped off in the render.</li>
                <li>Has the axes as defined below, with Z up, X towards the
                camera, and Y pointing to the right.</li>
                <li>The model should be the only object in the scene other than
                the camera and the light source.  There should be no background
                plane or extraneous objects regardless of whether or not they
                are set as renderable, visible, or not.</li>
            </ul>

        </dd>

        <dt>&rsaquo; After rendering, the background looks dark. What is causing
        that?</dt>
        <dd>
            <p>
                Check the color management of the .blend file and make sure that
                sRGB is selected. See <a
                    href="http://wiki.blender.org/index.php/Dev:Ref/Release_Notes/2.64/Color_Management">Color
                Management</a> for a discussion of the values. Often, the value is set
            to none which does not work. The other values have not been tested
            thoroughly.
            </p>
        </dd>

        <dt>&rsaquo; The model does not change
        orientations, the model does not appear sometimes, or only parts of the
        model are rotated?</dt>
        <dd>
            <p>
                Open the model in Blender and check that the object is selected in
                the Outliner. If the object is not selected in the Outliner, then
                the rotations used to set the orientation will not be applied to the
                model; they will be applied to something else. If the something
                else is the camera, then the camera will be rotated away from the
                object and it will not appear in the composite. If only part of the
                object is rotated, then it means that the entire object is not
                selected. You might have to create a <a
                    href="http://wiki.blender.org/index.php/Doc:2.6/Manual/Modeling/Objects/Groups_and_Parenting">parent
                object</a> that has all the parts under it. Be sure to select the
            parent object.
            </p>
        </dd>
    </dl>
</div>


<div class="container">
    <h2>Locations</h2>
        <a name="locations"></a>
    <dl>
        <dt>&rsaquo; Where is the model placed in the background?</dt>
        <dd>
            <p>Where the model is placed depends on what you have selected as the
            location type:</p>

            <ul>
                <li><b>Centered:</b> The center of the model is placed in the center of the background,
                as determined by dividing the width and height of the background in half.</li>

                <li><b>Random:</b>  The center of the model is place randomly in the background.  This means the model
                can extend beyond the boundary of the background, so that only part of it is visible.</li>

                <li><b>Point:</b> The center of the model is placed at the user-selected point.  To set the point, click
                on the background.  A marker will be placed at that point in the background, though the image of the model
                does not move.</li>

                <li><b>Multiline:</b> The center of the model is on the (possibly multi-point) line.  Click
                on the background to set sequential points in the multi-point line.  To remove a point from the line,
                click on it a second time.</li>

                <li><b>Rectangle:</b> The center of the model will be placed within the user-selected rectangle.  To 
		create the rectangle, first click to set the top left of the rectangle, and a second time to set the bottom right.
		</li>

                <li><b>Polygon:</b> The center of the model is placed uniformly in the user-selected area.  Click on the
                background to set points defining the area.  To remove a point from the area, click on it a second
                time.</li>
            </ul>

            <p>To reset a point, line, or area, simply select one of the
            other location types and then re-select point, line, or area.</p>
        </dd>
    </dl>
</div>

<div class="container">
    <h2>Orientations (Points of View, Aspects, and Numbers of Images)</h2>
    <dl>
        <dt>&rsaquo; What coordinate system are you using?</dt>
        <dd>
            <p>Short answer: SIGHTT uses the Blender coordinate system: right-handed, Z-up.
            To provide views of all sides of an object, SIGHTT rotates the object to
            produce an orientation, i.e. the part of the object that the camera is at.
            The orientation angles are what is commonly known as yaw, pitch, and roll.
            Withing SIGHTT, the yaw angle is &theta; measured from the X-axis in the XY
            plane, with positive counter-clockwise towards the Y-axis; then the pitch,
            or &phi; angle, measured from XY plane toward the Z-axis; finally roll is
            &omega;, the rotation along the body axis of the object.</p>

            <p>A couple of notes: the pitch angle is measured positive toward Z; that is,
            'nose up' is positive, even though from a right-hand-rule point of view, it
            should be down.  Also, internally, the values are all radians, but people
            seem to have a better intuitive grasp of degrees, so output is in degrees.
            Finally, we are rendering using Blender which needs a camera placed in the
            scene, and the initial point of view is assumed to be on the negative Y-axis,
            looking at the XZ plane.  See images below for what it looks like.</p>

            <p>Longer explanation: We need a consistent location (Cartesian) coordinate
            system between the web site, mathematical calculations, rendering /
            compositing (using Blender), and other code (Java), for both 3D models and
            images.  We describe orientations in angular coordinates, so we have to
            define that too. There are multiple ways of doing these things, and they do
            not agree:</p>

            <ul>
                <li>In Java3D, the coordinate system is a "right-handed,
                y-up" system, so that +y is local gravitational up, +x is to the
                right, and +z is towards the user, as defined in the <a
                        href="http://download.java.net/media/java3d/javadoc/1.4.0/javax/media/j3d/doc-files/VirtualUniverse.html">Java3D
                    docs</a>. Rotations are expressed as rotX (or rotY or rotZ), which
                are counter-clockwise about the X-axis (or Y or Z, respectively)
                in radians.
                </li>
                <li>In Blender, the coordinate system is "right-handed, z-up" but the
                default point of view is oblique to the axes. When you start Blender
                without a model file, you get a cube with length 1 at (0,0,0) and an XY
                plane for orientation. The point of view is at a positive X, negative Y,
                and positive Z location as shown below.  Rotations are similar to
                Java3D.  <br/><img src="${resource(dir: 'images/faq', file:
                        'defaultBlender.png')}" alt="BlenderImage"/>
                </li>
                <li>Mathematicians generally use a "right-handed, z-up"", coordinate
                systems. For angles, they generally use spherical coordinates where
                &theta; is measured in the XY plane from the X-axis and positive is
                towards the Y-axis, and &phi; is measured from the Z-axis (vertical)
                toward the XY plane. See the
                    <a href="http://en.wikipedia.org/wiki/Spherical_coordinate_system">Spherical
                    Coordinate System Wikipedia page</a>. Note that this definition of &phi;
                    is different from ours.  SIGHTT uses what could be called a
                    'geographical' or 'airplane' coordinate system, which &phi; is out of
                the XY plane.  Geographical coordinates use latitude, or elevation from
                the equator (i.e. the degrees <b>towards</b> the Z-axis).  Airplane
                coordinates would call pitch out of the plane, with up being positive.
                </li>
                <li>In 2D computer graphics, image coordinates start at the
                top left at (0,0), with the X-axis going to the right, and the
                Y-axis going down. Rotation is clockwise.</li>
            </ul>

            <p>So: SIGHTT uses the Blender coordinate system: right-handed, Z-up but initial
            point of view is assumed to be on the negative Y-axis, looking at the XZ
            plane.  The orientation of an object in a render has 3 rotations; &theta;
                from the X-axis in the XY plane, with positive counter-clockwise towards the
                Y-axis; &phi; measured from the plane (positive toward the Z-axis); and
            &omega; the rotation along the angle.</p>

            <p>Given an orientation of (0, 0, 0), the object is viewed from the negative
            Y-axis, with the object 'looking right' as shown below.  The camera (point
            of view for the render) is on the left of the scene, partly out of the
            image.</p>

            <img src="${resource(dir: 'images/faq', file: 'monkey_1_straight_ahead.png')}"
                 height="200" alt="BlenderImage"/>


            <p>If the orientation is (45, 45, 30), then the object goes through several
            rotations to orient it to the correct values.  First, it is rotated for yaw,
            or &theta; = 45 as shown below.</p>

            <img src="${resource(dir: 'images/faq', file: 'monkey_2_yaw45.png')}"
                 height="300" alt="BlenderImage"/>

            <p>Then the object is rotated by pitch or &phi; = 45</p>

            <img
                    src="${resource(dir: 'images/faq', file: 'monkey_3_yaw45_pitch45_withRing.png')}"
                    height="300" alt="BlenderImage"/>

            <p>Finally, the object is rotated by 30 degrees in roll or &omega; = 30.</p>

            <img
                    src="${resource(dir: 'images/faq', file: 'monkey_4_yaw45_pitch45_roll30_withRing.png')}"
                    height="300" alt="BlenderImage"/>

        </dd>

        <dt>&rsaquo; What orientations are produced as a function of number of images?</dt>
        <dd>
            <p>
                If a user wants 1000 images, then we create orientations that evenly
                distributed over the sphere with approximately constant angular separation
                between them. We use the algorithm for the points from
                <a href="http://blog.marmakoide.org/?p=1">Vogel's method</a> as implemented
            and described by Marmakoide. Essentially, the method uses the Golden Angle
            to form a spiral on the sphere, similar to the way that you can use the
            Golden Angle to lay points out in a disc. This algorithm defines &theta; and
            &phi;.  Then, for each (&theta; , &phi;) pair, we perform rotations equal to
            360 degrees divided by the angular separation between orientations.
            </p>

            <p>
                We have pre-computed the numbers of images you get for a certain separation
                between orientations.  If you would like 30 degree separation between views
                of the object, then you need 492 images.  That's because you need 41 (&theta;
                , &phi;) pairs to evenly distribute them on the sphere with 30 degrees
            between them; then, you need 12 rotations (i.e., 360/30) for each pair.  The   (&theta;
                , &phi;) pairs are shown below (only the ones on forward side of the sphere are shown).</p>
            <img
                    src="${resource(dir: 'images/faq', file: '41Points_30Degrees.png')}"
                    height="450" alt="BlenderImage"/>
        </dd>

        <dt>&rsaquo; Couldn't you just rotate azimuth and elevation by some
        increment, like latitude and longitude on a globe?</dt>
        <dd>
            <p>No, because that leads to an uneven distribution of orientations on the
            sphere. Think about the distance between longitudes for different
            latitudes; they get closer as you near the poles. What we need is a
            way to make the points maintain separation as they get closer to
            the poles, and that's what a spiral does. See this (archived) <a
                    href="http://web.archive.org/web/20120421191837/http://www.cgafaq.info/wiki/Evenly_distributed_points_on_sphere">
                page</a>. The "Distributing Points on Sphere" problem actually has a
            long and interesting history and is not a trivial problem.</p>
        </dd>
    </dl>
</div>
</div>
</div>
</body>
</html>
