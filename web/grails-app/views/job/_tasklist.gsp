<tr id="task${it.id}">
    <td style="vertical-align: middle">Result ${it.taskNumber}</td>
    <td style="vertical-align: middle">
        <g:if test="${it.compositeImage?.thumbnail?.valid}">
            <g:link controller="imageDisplay" action="display" params='[filePath: "${it.compositeImage.filePath}"]'><img
                    src="${compositeImageThumbnailSrcMap[it.compositeImage.id]}"
                    alt="Composite Image ${it.compositeImage.id}"/></g:link>
        </g:if>
        <g:elseif test="${it.running}">
            <p>Running...<r:img uri="/images/spinner.gif"/></p>
        </g:elseif>
        <g:elseif test="${it.error}">
            Failed
        </g:elseif>
        <g:else>
            Not Started
        </g:else>
    </td>
</tr>
