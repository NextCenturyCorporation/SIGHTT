<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Ingest New Model Configuration</title>
</head>

<body>
<g:uploadForm action="ingest">
    <fieldset class="form">
        <g:render template="form"/>
    </fieldset>

    <div id="button_row">
        <fieldset class="buttons">
            <g:submitButton name="create" class="save" value="Ingest"/>
        </fieldset>
    </div>
</g:uploadForm>
</body>
</html>    
