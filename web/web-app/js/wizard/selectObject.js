function selectModel(id, backgroundLink, modelLink) {
    $.post(backgroundLink, function (backgroundAmazonLink) {
        $.post(modelLink, function (modelAmazonLink) {
            $('#modelId').val(id);
            $("[name='next']").removeClass("sightt-disabled");
            $("[name='next']").removeAttr("disabled");
            resetScale();
            refreshCanvas(backgroundAmazonLink, modelAmazonLink);
        });
    });
}

function setModelName() {
    $("#name").val($("input[name=objectModel]").val().split('/').pop().split('\\').pop());
}

function storeScale(scale) {
    var newScale = Math.round(scale * 1000) / 1000;
    $('#scaleData').val(newScale);
}
