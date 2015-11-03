<p>
    This job is
    <g:formatNumber number="${((jobInstance.numComplete * 1.0) / (jobInstance.numTasks * 1.0)) * 100.0}" type="number"
                    format="###.##"/>
    % complete (${jobInstance.numComplete}/${jobInstance.numTasks})
    <span id="zipfile">
        <g:if test="${jobInstance.status == com.ncc.sightt.JobStatus.COMPLETE}">
            <g:link action="getZipFile" id="${jobInstance.id}">Get results as a .zip</g:link> (approx.
            <g:if test="${jobInstance.zipFileSize < 1024}">
                <g:formatNumber number="${jobInstance.zipFileSize}" type="number" maxFractionDigits="2"/> Bytes)
            </g:if>
            <g:elseif test="${jobInstance.zipFileSize < 1024 * 1024}">
                <g:formatNumber number="${jobInstance.zipFileSize / 1024}" type="number" maxFractionDigits="2"/> KBytes)
            </g:elseif>
            <g:else>
                <g:formatNumber number="${jobInstance.zipFileSize / (1024 * 1024)}" type="number"
                                maxFractionDigits="2"/> MBytes)
            </g:else>
        </g:if>
        <g:elseif test="${jobInstance.numComplete == jobInstance.numTasks}">
            Generating Zipfile...<r:img uri="/images/spinner.gif"/>
        </g:elseif>
    </span>
</p>
