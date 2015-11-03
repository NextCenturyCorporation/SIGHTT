<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Super Secret Force Delete Page!!!</title>
    <r:script>
function doDelete(link) {
    $.post(
    "<g:createLink controller="job" action="delete" id="${id}"/>",
    function(resp) {
        $('body').html(resp);
    });
}
    </r:script>

</head>

<body>
<p>Deleting job ${id} by force!</p>
<a href="#" onclick="doDelete();">Click here to delete the job</a>
</body>
