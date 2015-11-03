<div><b>Validation Result:</b></div>
<g:if test="${model.status == com.ncc.sightt.ModelStatus.FAILED}">
  <div id="status">FAILURE</div></g:if><g:else>
  <div id="status">SUCCESS</div>

  <div id="image"></div></g:else>
<div id="blender">
  <span id="modelImage"><r:img uri="/images/spinner.gif"/></span>

  <div id="stdout"><p class="header"><b>Blender Standard Output:</b></p></div>

  <div class="code">
    ${model.output.stdout.replaceAll("\r\n|\n|\r", "<br/>")}
  </div>

  <g:if test="${model.output.stderr}">
    <div id="stderr"><p class="header"><b>Blender Standard Error:</b></p>

      <p>${model.output.stderr.replaceAll("\r\n|\n|\r", "<br/>")}</p>
    </div>
  </g:if>

  <div id="error">
    <g:if test="${model.output.error}">
      <p class="header"><b>Error Message:</b> &nbsp ${model.output.error}</p>
    </g:if>
  </div>

  <div id="exitval">
    <p class="header"><b>Exit Code:</b> &nbsp ${model.output.exitValue}</p>
  </div>
</div>
