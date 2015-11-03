
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class BlenderCallerTest {

    File model
    File script

    static final String TEST_SHORT_STDOUT = " short stdout "
    static final String TEST_LONG_STDOUT =
        """Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor
    incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud 
    exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute 
    irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla 
    pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia 
    deserunt mollit anim id est laborum."""
    static final String fakeModel = "fake.blend"
    static final String fakeScript = "fake.py"

    final shouldFail = new GroovyTestCase().&shouldFail

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    void setUp() {
        model = folder.newFile(fakeModel)
        script = folder.newFile(fakeScript)
    }

    @After
    void tearDown() {
        model.delete()
        script.delete()
    }

    @Test
    void testCollectResultsShort() {
        BlenderCaller caller = new BlenderCaller()
        caller.stdoutBuffer = new StringBuffer(TEST_SHORT_STDOUT)
        caller.stderrBuffer = null
        BaseRenderMessage message = caller.collectResults(new BaseRenderMessage())
        assert message.stdout == TEST_SHORT_STDOUT
        assert message.stderr.isEmpty()
    }

    @Test
    void testCreateCommand() {
        BlenderCaller caller = new BlenderCaller()
        String command = caller.createCommand(model.absolutePath,
                script.absolutePath)
        assert BlenderCaller.BLENDER_EXECUTABLE +
                " -b ${model.absolutePath} -P ${script.absolutePath}" == command
    }

    @Test
    void testRenderObjectProcessFailureInRunCommand() {
        BlenderCaller caller = new BlenderCaller()
        BaseRenderMessage message = caller.runCommand("brokenCommand",
                new BaseRenderMessage())
        assert !message.error.isEmpty()
    }

    @Test
    void testRenderObjectSuccess() {
        // TODO
    }

    @Test
    void testRenderObjectProcessFailure() {
        BlenderCaller caller = new BlenderCaller()
        def testString = "testRenderObjectSucessFakeCmd"
        try {
            byte[] inArray = "InputStream".getBytes();
            byte[] errArray = "ErrorStream".getBytes();
            def inStream = new ByteArrayInputStream(inArray);
            def errStream = new ByteArrayInputStream(errArray);
            def mockProcess = [getInputStream: { inStream }, getErrorStream: { errStream },
                    waitFor: {-> return 0 }, exitValue: {-> return 1 }] as Process

            testString.metaClass.execute = { return mockProcess }

            BaseRenderMessage message = caller.runCommand(testString,
                    new BaseRenderMessage())
            message = caller.collectResults(message)

            assert message
            assertNotNull message.error
            assertNotNull message.stdout
            assertNotNull message.stderr
            assert !message.error.isEmpty()
            assertTrue "Exit value was zero", message.exitValue != 0
        }
        finally {
            // Use finally block here because the .metaclass will stick around
            testString.metaClass = null
        }
    }

    @Test
    void testContainsError() {
        BlenderCaller caller = new BlenderCaller()
        assert caller.containsError("error")
        assert caller.containsError("Error")
        assert caller.containsError("ERROR")
        assert !caller.containsError("success")
        assert !caller.containsError("")
    }

    @Test
    void testCleanStderrGeometryData() {
        BlenderCaller caller = new BlenderCaller()
        String stderr = "A bunch of stuff here\nError: Object does not have geometry data\nMore stuff here"
        String newErr = caller.cleanStdErr(stderr)
        assert !caller.containsError(newErr)
    }


    @Test
    void testCleanStderrALSA() {
        BlenderCaller caller = new BlenderCaller()
        String stderr = "A bunch of stuff here\nALSA: Horrible error\nMore stuff here"
        assert caller.containsError(stderr)

        String newErr = caller.cleanStdErr(stderr)
        assert !caller.containsError(newErr)
    }
}
