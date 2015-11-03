function storeSelectNumberInfo() {
    var rotVal = $("input[name='rotGroup']:checked").val();
    $('#spacing').val(rotVal);
    return true; //This means the submission was validated (we don't actually validate it)
}

function hideElement(name) {
    $("#" + name).hide();
}
