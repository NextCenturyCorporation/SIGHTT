<g:applyLayout name="main">
    <h2>Your settings have been updated!</h2>
    <table>
        <tr><td>Instance Type</td><td>${config.instanceType}</td></tr>
        <tr><td>Instance Image</td><td>${config.instanceBaseId}</td></tr>
        <tr><td>Instance Capacity</td><td>${config.instanceCapacity}</td></tr>
        <tr><td>Instance UserData</td><td>${config.instanceDataFile}</td></tr>
    </table>

    <p><g:link action="changeSettings">Click Here</g:link> to change settings again</p>

    <p><g:link action="index">Click here</g:link> to view the instances</p>

    <p><g:link action="requestInstances">Click here</g:link> to start instances with these new settings</p>
</g:applyLayout>