package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Language
import com.example.data.model.PracticeDataset
import com.example.ui.editor.SyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CodeFix AI", appName)
    }

    @Test
    fun `verify all 7 languages supported`() {
        assertEquals(7, Language.entries.size)
        assertNotNull(Language.fromId("python"))
        assertNotNull(Language.fromId("c"))
        assertNotNull(Language.fromId("cpp"))
        assertNotNull(Language.fromId("java"))
        assertNotNull(Language.fromId("javascript"))
        assertNotNull(Language.fromId("html"))
        assertNotNull(Language.fromId("css"))
    }

    @Test
    fun `verify practice challenges dataset`() {
        assertTrue(PracticeDataset.challenges.isNotEmpty())
        val first = PracticeDataset.challenges.first()
        assertNotNull(first.title)
        assertNotNull(first.description)
        assertTrue(first.starterCodes.isNotEmpty())
    }

    @Test
    fun `verify syntax highlighter highlights code without crash`() {
        val pythonCode = "def hello():\n    return 'world'"
        val highlighted = SyntaxHighlighter.highlight(pythonCode, Language.PYTHON)
        assertEquals(pythonCode, highlighted.text)
    }
}

