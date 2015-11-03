<g:applyLayout name="main">
    <p>This page allows you to change the settings for starting new instances</p>
    <g:form name="settings" action="updateSettings">
        <table>
            <tr><td>Select the instance Type:</td><td><g:select name="type" from="['m1.small', 'm1.large', 'm1.xlarge']"
                                                                value="${config.instanceType}"/></td></tr>
            <tr><td>Set the instance Capacity:</td><td><g:textField name="capacity"
                                                                    value="${config.instanceCapacity}"/></td></tr>
            <tr><td>Set the instance BaseID (AMI):</td><td><g:textField name="ami"
                                                                        value="${config.instanceBaseId}"/></td></tr>
            <tr><td>Set the JSON user data to send:</td><td><g:select name="userdata"
                                                                      from="['taskconsumer_concurrent.json', 'taskconsumer_x8.json']"
                                                                      value="${config.instanceDataFile}"/></td></tr>
            <tr><td><g:submitButton name="submit"/></td></tr>
        </table>
    </g:form>
</g:applyLayout>