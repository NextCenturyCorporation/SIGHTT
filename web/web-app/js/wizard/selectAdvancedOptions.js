function setupAdvancedOptions(generateMasks) {
    if (generateMasks === "true") {
        $('#generateMasksString').val("true");
        $('#generateMasksCheckbox').attr("checked", "checked");
    }
}

function storeAdvancedOptions() {
    if ($("#customNumberOfImages").val()) {
        var numberOfImagesValue = parseInt($("#customNumberOfImages").val());
        if (numberOfImagesValue !== NaN) {
            $("#numImages").val(numberOfImagesValue);
        }
    }

    var imageTypeValue = $("input[name='imageTypeGroup']:checked").val();
    $('#imageTypeString').val(imageTypeValue);

    var orientationTypeValue = $("input[name='orientationTypeGroup']:checked").val();
    $('#orientationTypeString').val(orientationTypeValue);
}

function setGenerateMasks(checked) {
    $('#generateMasksString').val(checked);
}
