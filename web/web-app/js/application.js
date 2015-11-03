if (typeof jQuery !== 'undefined') {
    (function ($) {
        $('#spinner').ajaxStart(function () {
            $(this).fadeIn();
        }).ajaxStop(function () {
                $(this).fadeOut();
            });
    })(jQuery);
}


// Global fix for console issues in IE.  Taken from http://stackoverflow.com/questions/7585351/testing-for-console-log-statements-in-ie
// which in turn got it from twitter's source code. 
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

// function fixConsole(alertFallback) {
//     if (typeof console === "undefined") {
//         console = {}; // define it if it doesn't exist already
//     }
//     if (typeof console.log === "undefined") {
//         if (alertFallback) {
//             console.log = function (msg) {
//                 alert(msg);
//             };
//         }
//         else {
//             console.log = function () {
//             };
//         }
//     }
//     if (typeof console.dir === "undefined") {
//         if (alertFallback) {
//             // THIS COULD BE IMPROVED… maybe list all the object properties?
//             console.dir = function (obj) {
//                 alert("DIR: " + obj);
//             };
//         }
//         else {
//             console.dir = function () {
//             };
//         }
//     }
// }
// 
// fixConsole(false);
