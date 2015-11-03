<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="main">
    <r:require module="defineGeometryDrawing"/>
    <title>Draw a coordinate system</title>
</head>

<body>

<!-- Add the menu bar. -->
<div class="nav" role="navigation">
    <ul>
        <li>
            <a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
        </li>
        <li>
            <a class="create" onclick="save();" href="javascript:void(0);">Save Geometry</a>
        </li>
        <li>
            <a class="delete" href="${createLink(action: 'deleteGeometry', id: id)}">Delete Geometry</a>
        </li>
    </ul>
</div>

<div id="lengthPicturedDiv" style="width:200px; border:1px solid black; display:none; background-color:#FFFFFF;">
    Length in meters: <input type="text" size="5" name="lengthInMeters" id="lengthInMeters"> </br>
    <button type="button" onclick="applyLength();">Apply</button> <button type="button"
                                                                          onclick="cancelLength();">Cancel</button>
</div>

<div id="canvas"></div>

<script type="text/javascript">
    backgroundUrl = "${backgroundUrl}";
    postUrl = "${createLink(controller:'defineGeometry', action:'saveGeometry', id:id)}";
    scalingUrl = '${resource(dir:'images',file:'transparent_stick.gif')}';

    var geometry = '${geometryJson}';
    createGeometry(geometry);
</script>

</body>
</html>

