<%@ page import="com.ncc.sightt.auth.Permissions" %>

<g:applyLayout name="wizard">
    <head>
        <title>Job Settings</title>
        <r:require module="wizardJobSettings"/>
        <r:script>
            function saveJobSettings() {
                var jobSettingsData = JSON.stringify({
                    'jobName': $("#jobName").val(),
                    'imageType': $("#imageType").val(),
                    'generateAllMasks': $("#generateMasks").prop("checked"),
                    'private': $("#private").is(":checked")
                });
                $("#data").val(jobSettingsData);
                return true;
            }
        </r:script>
    </head>

    <content tag="step">
        <div class="step">
            <span>Step 1</span>
            Backgrounds
        </div>

        <div class="step">
            <span>Step 2</span>
            3D Model
        </div>

        <div class="step">
            <span>Step 3</span>
            Location
        </div>

        <div class="step">
            <span>Step 4</span>
            Complexity of Perspectives
        </div>

        <div class="step active">
            <span>Step 5</span>
            Job Settings
        </div>

        <div class="final step">
            <span>Step 6</span>
            Summary
        </div>
    </content>

    <content tag="background">
        <div class="jobsettings">
            <ul>
                <li>
                    <label for="jobName">Job Name:</label><g:textField name="jobName"></g:textField>
                </li>
                <li>
                    <label for="imageType">Image Type</label><g:select name="imageType"
                                                                       from="${validImageTypes}"></g:select>
                </li>
                <li>
                    <label for="generateMasks">Generate Masks for All Layers</label><g:checkBox
                        name="generateMasks"></g:checkBox>
                </li>
                <li>
                    <label for="private">Private Job?</label><g:checkBox name="private"
                                                                         checked="${defaultPrivacy == Permissions.PRIVATE}"/>
                </li>
            </ul>
        </div>
    </content>

    <content tag="description">
        Complete the configuration for your job.<br/>
        In the next step, we will show a summary of your job which you can review before submitting it to SIGHTT for processing.
    </content>

    <content tag="left">
    </content>

    <content tag="nav">
        <!--
hiddenFields without a value are set from THIS page, otherwise their values are passed in from previous pages in the wizard process
 -->
        <g:form name="nav" action="finishJobSettings" onsubmit="return saveJobSettings();">
            <g:hiddenField id="data" name="data" value=""/>
            <g:submitButton name="next" class="save  wizard-next-button" value="Continue"/>
        </g:form>
    </content>

</g:applyLayout>

