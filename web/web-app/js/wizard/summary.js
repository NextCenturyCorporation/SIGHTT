var submitted = false;

function disableButton(button) {
    button.attr('disabled', 'disabled');
    button.addClass('sightt-disabled');
}

function doStartJob() {
    if (!submitted) {
        submitted = true;
        var nextSelector = $('input[name=next]');
        var advancedSelector = $('input[name=advanced]');
        var privateSelector = $('input[name=private]');
        disableButton(nextSelector);
        disableButton(advancedSelector);
        nextSelector.val('Submitting Job...');
        $('input[name=_private]').val(privateSelector.is(':checked'));
    }
}
