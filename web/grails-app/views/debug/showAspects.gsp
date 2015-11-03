<%--
  Created by IntelliJ IDEA.
  User: abovill
  Date: 4/24/14
  Time: 4:05 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="popup"/>

    <title>Show aspects for job ${jobId}</title>
    <r:require module="limitedaspects"/>
    <r:script>
        var modelView;
        var aspectHandler;
        window.onload = function() {
            modelView = new ModelView("modelview");
            modelView.setSrcPath("${createLink(action: 'index', controller: 'main', absolute: true)}js/lib/modelview");
            modelView.loadModel("${modelLocation}");
            //Load cameras asynchronously!
            $("#cameraprogress").html("Loading Cameras");
            var camListUrl = "${createLink(action: 'cameraList', absolute: true, params: ['numPoints': numPointsInJob])}";
            var cameras = ${activeCameras};
            aspectHandler = new AspectHandler();
            aspectHandler.enableHandlers=false;
            $.post(camListUrl, function(data,textStatus,jqXHR) {
                aspectHandler.init(modelView,data);
                for (var idx = 0; idx < cameras.length; idx++) {
                    aspectHandler.aspects[idx] = {active: cameras[idx]}
                    aspectHandler.dirtyList.push(idx);
                    var event = new Event('updateDirtyCameras');
                    aspectHandler.sceneHandler.container.dispatchEvent(event);
                }

               $("#cameraprogress").hide();
            });
        }
    </r:script>
</head>

<body>
<div id="modelcontainer" class="ui-block">
    <div id="progressbar"></div>

    <div id="cameraprogress"></div>

    <div id="modelview"></div>
</div>
</body>
</html>