function loadThumbnail(id, loadUrl) {
    $.post(
        loadUrl,
        function (msg) {
            if (msg == "NOTREADY") {
                setTimeout(function () {
                    loadThumbnail(id, loadUrl)
                }, 10000);
            } else {
                $("#" + id).html("<img src=\"" + msg + "\"/>");
            }
        });
}

function fixLink(thumbId, thumbUrl, linkId, linkUrl) {
    $.post(
        linkUrl,
        function (msg) {
            if (msg == "NOTREADY") {
                setTimeout(function () {
                    fixLink(thumbId, thumbUrl, linkId, linkUrl)
                }, 10000);
            } else {
                var tmpLink = "<a href='" + msg + "'><span id='" + thumbId + "'></span>";
                $("#" + linkId).html(tmpLink);
                loadThumbnail(thumbId, thumbUrl);
            }
        });
}

function getModelInfo(id, linkUrl, thumbId, thumbUrl) {
    $.post(
        linkUrl,
        function (msg) {
            if (msg == "NOTREADY") {
                setTimeout(function () {
                    getModelInfo(id, linkUrl, thumbId, thumbUrl)
                }, 5000);
            } else {
                $("#" + id).html(msg.content);
                if (msg.success == true) {
                    loadThumbnail(thumbId, thumbUrl);
                } else {
                    $('#' + thumbId).html("");
                }
                runWhenModelInfoIsReady();
            }
        });
}

// Method that can be overridden.
function runWhenModelInfoIsReady() {
}

function triggerThumbnailGeneration(loadUrl) {
    $.post(
        loadUrl,
        function (msg) {
        }
    );
}

function loadTaskList(id, loadUrl) {
    var isComplete = false;
    $.post(
        loadUrl,
        function (msg) {
            $("#" + id).html(msg.html);
            if (msg.complete != true) {
                setTimeout(function () {
                    loadTaskList(id, loadUrl);
                }, 2000);
            }
        }
    );
}

function updateProgress(data) {
    $.post(
        data,
        function (msg) {
            $("#progressMeter").html(msg.progress);
        });
}

function eventLoadTask(taskData) {
    var url = taskData.link;

    $.post(
        url,
        function (msg) {
            var rowId = "#task" + taskData.id;
            $("#task" + taskData.id).replaceWith(msg.html);
            $("#progressMeter").html(msg.progress);
        });
}

function updateZipFile(data) {
    $("#zipfile").html(data);
}


