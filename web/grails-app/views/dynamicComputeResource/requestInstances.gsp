<g:applyLayout name="main">

    <div>
        <h1>Instances will be created with the following settings:</h1>
        <table>
            <tr><td>Instance Type</td><td>${config.instanceType}</td></tr>
            <tr><td>Instance Image</td><td>${config.instanceBaseId}</td></tr>
            <tr><td>Instance Capacity</td><td>${config.instanceCapacity}</td></tr>
            <tr><td>User Data</td><td>${config.instanceDataFile}</td></tr>
        </table>

        <p>If you want to change these values, <g:link action="changeSettings">click here</g:link></p>
    </div>

    <p>How many instances would you like to start?</p>

    <form action="startInstances" method="post">
        <g:textField name="numInstances" type="number" value="1"/>
        <g:submitButton name="submit"/>
    </form>
</g:applyLayout>
