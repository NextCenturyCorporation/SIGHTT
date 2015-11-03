
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
package com.ncc.sightt

import com.ncc.sightt.message.BaseRenderMessage
import groovy.util.logging.Log4j
import org.apache.commons.lang3.StringUtils

import java.util.regex.Pattern

/**
 * Call the blender executable, received any results or output, set the message values, and 
 * handle any errors.
 */
@Log4j
class BlenderCaller {

    static final String BLENDER_EXECUTABLE = '/opt/blender/blender'
    // Length of a SQL LONGVARCHAR
    static final OUTPUT_TEXT_MAX_LENGTH = 32000

    // Note:  Have to use string buffers to capture output from process, because the process can fill
    // the buffer up, and then the process will lock
    StringBuffer stderrBuffer = new StringBuffer()
    StringBuffer stdoutBuffer = new StringBuffer()

    def renderWithBlender(String modelPath, String scriptPath, BaseRenderMessage message) {
        stdoutBuffer.delete(0, stdoutBuffer.length())
        stderrBuffer.delete(0, stderrBuffer.length())

        String command = createCommand(modelPath, scriptPath)
        message.error = ""
        message.stdout = ""
        message.stderr = ""
        BaseRenderMessage finishedMessage = runCommand(command, message)

        finishedMessage = collectResults(finishedMessage)

        return finishedMessage
    }

    def createCommand(String modelPath, String scriptPath) {
        return """${BLENDER_EXECUTABLE} -b ${modelPath} -P ${scriptPath}"""
    }

    def runCommand(String command, BaseRenderMessage message) {
        log.debug "Running:  ${command}"

        try {
            Process process = command.execute()
            process.waitForProcessOutput(stdoutBuffer, stderrBuffer)
            message.exitValue = process.exitValue()
        }
        catch (Exception e) {
            log.error "Error running blender:  ${e.message}"
            message.error = e.message
        }
        return message
    }

    def collectResults(BaseRenderMessage message) {
        message.stdout = stdoutBuffer ? StringUtils.abbreviateMiddle(stdoutBuffer.toString(), "\n...\n", OUTPUT_TEXT_MAX_LENGTH) : ""
        message.stderr = stderrBuffer ? StringUtils.abbreviateMiddle(stderrBuffer.toString(), "\n...\n", OUTPUT_TEXT_MAX_LENGTH) : ""
        message.stderr = cleanStdErr(message.stderr)

        if (message.exitValue != 0 || containsError(message.stderr)) {

            log.error "Error running blender:  Exit Value ${message.exitValue}"
            message.error = message.error + "Possible error running blender: ${message.exitValue}"
        }
        return message
    }

    /**
     * Stderr may have things that we do not consider to be real errors, remove them
     */
    def cleanStdErr(String stderrMessage) {

        if (!stderrMessage)
        {
            return ""
        }

        // Ignore this error message because it is a warning that blender outputs
        // while exporting a model as an object (.obj) file.
        String blenderExportWarning = "Error: Object does not have geometry data"
        String text = stderrMessage.replace(blenderExportWarning, "[Removed geometry data warnings]")

        // Ignore errors where the line begins with Alsa (sound)
        StringBuffer sb = new StringBuffer()
        String[] stderrLines = text.split("\n")
        Pattern p = Pattern.compile("^ALSA:.*")
        for (String line : stderrLines) {
            if (line.matches(p)) {
                line = "[Removed ALSA warning]"
            }
            sb.append(sb + "\n")
        }
        return sb.toString()
    }

    /**
     * See if the passed string contains (in some form), the word 'error'
     */
    def containsError(String stderrMessage) {
        return StringUtils.containsIgnoreCase(stderrMessage, "error")
    }
}
