var objectModelDropzone;

function createObjectModelDropzone(saveLink) {
    objectModelDropzone = new Dropzone(document.body, {
        url: saveLink,
        previewsContainer: "#uploadPreview",
        maxFiles: 1,
        clickable: "#browseButton",
        paramName: "objectModel",
        addRemoveLinks: true
    });

    objectModelDropzone.on("addedfile", function (file) {
        $('#uploadPreview').show();
        $('#browseButton').hide();
        //$("#uploadText").val(file.name);
    });

    objectModelDropzone.on("success", function (file) {
        $("#objectModelName").val(removeExtension(file.name));
        $('.dz-upload').html("Complete!");
        $('#createButton').prop("disabled", false);
    });

    objectModelDropzone.on("reset", function (file) {
        $('#browseButton').show();
        $('#objectModelName').val("");
        $('#createButton').prop('disabled', true);
    });

    objectModelDropzone.on("uploadprogress", function (file, progress, bytesSent) {
        $('.dz-upload').html("Progress: " + progress + "%");
    });

    objectModelDropzone.on("maxfilesexceeded", function (file) {
        objectModelDropzone.removeFile(file);
    });
}

function removeExtension(name) {
    return name.substring(0, name.indexOf("."));
}

function saveHiddenFields() {
    $("#modelTypeInput").val($("#modelType").val());
    $("#modelNameInput").val($("#objectModelName").val());
}
