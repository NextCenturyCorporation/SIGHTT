/* Functions shared between different parts of the wizard */

function loadCanvas(scaleData, backgroundLink, modelLink, callback) {
    $.post(backgroundLink, function (backgroundAmazonLink) {
        if (!modelLink) {
            loadResizeCanvas(scaleData, backgroundAmazonLink, null, callback);
            return;
        }

        $.post(modelLink, function (modelAmazonLink) {
            loadResizeCanvas(scaleData, backgroundAmazonLink, modelAmazonLink, callback);
        });
    });
}
