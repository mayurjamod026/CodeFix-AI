package com.example

import com.example.data.model.ExecutionStatus
import com.example.data.model.Language
import com.example.data.model.ProblemCategory
import com.example.service.GeminiAiService
import com.example.service.SandboxExecutionService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CodeFixCoreFeaturesTest {

    private val sandboxService = SandboxExecutionService()
    private val aiService = GeminiAiService()

    // 1. Valid Python code
    @Test
    fun testValidPythonCodeExecution() = runBlocking {
        val pythonCode = """
            print("Hello, Python!")
            a = 5
            b = 10
            print(f"Sum: {a + b}")
        """.trimIndent()

        val result = sandboxService.execute(pythonCode, Language.PYTHON)

        assertEquals(ExecutionStatus.SUCCESS, result.status)
        assertEquals(0, result.exitCode)
        assertNotNull(result.stdout)
        assertTrue("Stdout should contain 'Hello, Python!'", result.stdout!!.contains("Hello, Python!"))
        assertTrue("Stdout should contain 'Sum: 15'", result.stdout!!.contains("Sum: 15"))
    }

    // 2. Invalid C code
    @Test
    fun testInvalidCCodeCompilationError() = runBlocking {
        val invalidCCode = """
            #include <stdio.h>
            int main() {
                printf("Missing semicolon")
                return 0;
            }
        """.trimIndent()

        val result = sandboxService.execute(invalidCCode, Language.C)

        assertEquals(ExecutionStatus.COMPILATION_ERROR, result.status)
        assertEquals(1, result.exitCode)
        val errorText = (result.compilationOutput ?: "") + (result.stderr ?: "")
        assertTrue("Should report missing semicolon or syntax error", errorText.contains(";") || errorText.contains("error", ignoreCase = true))
    }

    // 3. JavaScript runtime error
    @Test
    fun testJavaScriptRuntimeError() = runBlocking {
        val jsRuntimeErrorCode = """
            console.log("Before error");
            throw new Error("Custom JS runtime error");
        """.trimIndent()

        val result = sandboxService.execute(jsRuntimeErrorCode, Language.JAVASCRIPT)

        assertEquals(ExecutionStatus.RUNTIME_ERROR, result.status)
        assertEquals(1, result.exitCode)
        assertNotNull(result.stderr)
        assertTrue("Stderr should contain runtime error message", result.stderr!!.contains("Custom JS runtime error"))
    }

    // 4. HTML preview
    @Test
    fun testHtmlPreviewRendering() = runBlocking {
        val htmlCode = """
            <!DOCTYPE html>
            <html>
            <head><title>CodeFix Sandbox</title></head>
            <body>
                <h1>Hello HTML Preview</h1>
                <p>Sandboxed render output</p>
            </body>
            </html>
        """.trimIndent()

        val result = sandboxService.execute(htmlCode, Language.HTML)

        assertEquals(ExecutionStatus.SUCCESS, result.status)
        assertEquals(0, result.exitCode)
        assertNotNull(result.htmlPreviewContent)
        assertTrue("htmlPreviewContent should contain HTML markup", result.htmlPreviewContent!!.contains("<h1>Hello HTML Preview</h1>"))
        assertNotNull(result.stdout)
        assertTrue("Stdout should report preview render", result.stdout!!.contains("HTML/CSS rendered successfully"))
    }

    // 5. AI Analyze
    @Test
    fun testAiAnalyzeDetectsProblems() = runBlocking {
        val buggyCode = """
            #include <stdio.h>
            int main() {
                int x = 10
                if (x = 10) {
                    printf("Equal\n");
                }
                return 0;
            }
        """.trimIndent()

        val analysis = aiService.analyzeCode(buggyCode, Language.C)

        assertNotNull(analysis)
        assertNotNull(analysis.summary)
        assertTrue("Quality score should be in 0..100 range", analysis.qualityScore in 0..100)
        assertTrue("Problems list should not be empty for buggy code", analysis.problems.isNotEmpty())
        val hasSyntaxOrLogical = analysis.problems.any {
            it.category == ProblemCategory.SYNTAX_ERROR || it.category == ProblemCategory.LOGICAL_ISSUE
        }
        assertTrue("Should detect syntax error or logical issue", hasSyntaxOrLogical)
    }

    // 6. AI Fix
    @Test
    fun testAiFixGeneratesCorrectedCode() = runBlocking {
        val pythonBuggy = """
            def calculate_total(a, b)
                return a + b
        """.trimIndent()

        val fixResult = aiService.fixCode(pythonBuggy, Language.PYTHON)

        assertNotNull(fixResult)
        assertNotNull(fixResult.originalIssue)
        assertTrue("Corrected code should include missing colon ':'", fixResult.correctedCode.contains("def calculate_total(a, b):"))
        assertTrue("changesMade should document changes", fixResult.changesMade.isNotEmpty())
        assertNotNull(fixResult.explanation)
    }

    // 7. Empty code
    @Test
    fun testEmptyCodeHandling() = runBlocking {
        // Test SandboxExecutionService handles empty code
        val execResult = sandboxService.execute("   ", Language.PYTHON)
        assertEquals(ExecutionStatus.SANDBOX_ERROR, execResult.status)
        assertNotNull(execResult.stderr)
        assertTrue("Should report empty code", execResult.stderr!!.contains("empty", ignoreCase = true))

        // Test AI Analyze handles empty code
        val analysisResult = aiService.analyzeCode("", Language.PYTHON)
        assertEquals(0, analysisResult.qualityScore)
        assertTrue(analysisResult.summary.contains("empty", ignoreCase = true))

        // Test AI Fix handles empty code
        val fixResult = aiService.fixCode("", Language.PYTHON)
        assertTrue(fixResult.explanation.contains("empty", ignoreCase = true))
    }

    // 8. Unsupported language
    @Test
    fun testUnsupportedLanguageHandling() = runBlocking {
        // Test passing null Language
        val nullLangResult = sandboxService.execute("print('hi')", null)
        assertEquals(ExecutionStatus.SANDBOX_ERROR, nullLangResult.status)
        assertNotNull(nullLangResult.stderr)
        assertTrue(nullLangResult.stderr!!.contains("Unsupported", ignoreCase = true))

        // Test passing unsupported language ID
        val unsupportedIdResult = sandboxService.executeByLanguageId("puts 'hello'", "ruby")
        assertEquals(ExecutionStatus.SANDBOX_ERROR, unsupportedIdResult.status)
        assertNotNull(unsupportedIdResult.stderr)
        assertTrue(unsupportedIdResult.stderr!!.contains("Unsupported", ignoreCase = true))
        assertTrue(unsupportedIdResult.stderr!!.contains("ruby", ignoreCase = true))
    }
}
