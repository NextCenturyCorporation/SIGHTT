function startListener(atmosUrl) {
    var transport = "websocket";
    var socket = $.atmosphere;

    var request = {
        url: atmosUrl,
        contentType: 'application/json',
        logLevel: 'debug',
        transport: transport,
        fallbackTransport: 'long-polling',
        reconnectInterval: 3,
    };


    request.onTransportFailure = function (errorMsg, request) {
        socket.info(errorMsg);
        if (window.EventSource) {
            request.fallbackTransport = 'sse';
            transport = 'sse';
        }
    };

    request.onOpen = function (response) {
    };

    request.onMessage = function (response) {
        var message = response.responseBody;
        try {
            var json = JSON.parse(message);
        }
        catch (e) {
            return;
        }
        try {
            if (json.type == "update") {
                //Update the progress with the link provided...
                updateProgress(json.link);
            }
            else if (json.type == "initialized") {
                var spinner = '<r:img uri="/images/spinner.gif"/>'
                $('#progressMeter').html('<p>Initialized.  Waiting for an available worker... ' + spinner + '</p>');
            }

        }
        catch (e) {
            return;
        }
    };

    request.onError = function (response) {
    };
    var subSocket = socket.subscribe(request);
};

