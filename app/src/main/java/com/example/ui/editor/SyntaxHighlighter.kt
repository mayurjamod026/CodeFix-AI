package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.Language

object SyntaxHighlighter {

    private val colorKeyword = Color(0xFF38BDF8) // Sky Blue
    private val colorType = Color(0xFFA78BFA)    // Purple
    private val colorString = Color(0xFF34D399)  // Emerald Green
    private val colorNumber = Color(0xFFFBBF24)  // Amber
    private val colorComment = Color(0xFF64748B) // Slate Gray
    private val colorOperator = Color(0xFFF43F5E)// Rose
    private val colorPreprocessor = Color(0xFFFB923C) // Orange
    private val colorHtmlTag = Color(0xFF38BDF8)
    private val colorHtmlAttr = Color(0xFFFCD34D)

    private val pythonKeywords = setOf(
        "def", "class", "return", "if", "elif", "else", "for", "while", "import",
        "from", "as", "try", "except", "finally", "with", "yield", "lambda",
        "pass", "break", "continue", "in", "is", "not", "and", "or", "None",
        "True", "False", "global", "nonlocal", "async", "await"
    )

    private val cCppKeywords = setOf(
        "int", "float", "double", "char", "void", "bool", "long", "short",
        "signed", "unsigned", "auto", "const", "static", "struct", "class",
        "public", "private", "protected", "virtual", "namespace", "using",
        "return", "if", "else", "for", "while", "do", "switch", "case",
        "default", "break", "continue", "goto", "sizeof", "new", "delete",
        "typedef", "include", "define", "ifdef", "ifndef", "endif"
    )

    private val javaKeywords = setOf(
        "public", "private", "protected", "class", "interface", "extends",
        "implements", "static", "final", "void", "int", "boolean", "double",
        "float", "char", "byte", "short", "long", "new", "return", "if",
        "else", "for", "while", "do", "switch", "case", "default", "break",
        "continue", "try", "catch", "finally", "throw", "throws", "import",
        "package", "this", "super", "null", "true", "false"
    )

    private val jsKeywords = setOf(
        "function", "const", "let", "var", "return", "if", "else", "for",
        "while", "do", "switch", "case", "default", "break", "continue",
        "class", "extends", "import", "export", "from", "as", "default",
        "new", "this", "super", "async", "await", "try", "catch", "finally",
        "throw", "typeof", "instanceof", "null", "undefined", "true", "false"
    )

    fun highlight(code: String, language: Language): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val text = code

            // 1. Comments
            when (language) {
                Language.PYTHON -> {
                    val singleComment = Regex("#.*")
                    singleComment.findAll(text).forEach { match ->
                        addStyle(
                            SpanStyle(color = colorComment, fontStyle = FontStyle.Italic),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
                Language.C, Language.CPP, Language.JAVA, Language.JAVASCRIPT, Language.CSS -> {
                    // Single line //
                    Regex("//.*").findAll(text).forEach { match ->
                        addStyle(
                            SpanStyle(color = colorComment, fontStyle = FontStyle.Italic),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                    // Multi line /* */
                    Regex("/\\*[\\s\\S]*?\\*/").findAll(text).forEach { match ->
                        addStyle(
                            SpanStyle(color = colorComment, fontStyle = FontStyle.Italic),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
                Language.HTML -> {
                    Regex("<!--[\\s\\S]*?-->").findAll(text).forEach { match ->
                        addStyle(
                            SpanStyle(color = colorComment, fontStyle = FontStyle.Italic),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
            }

            // 2. Strings ("..." or '...' or `...`)
            val stringPattern = Regex("""(".*?"|'.*?'|`.*?`)""")
            stringPattern.findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = colorString),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 3. Numbers
            val numberPattern = Regex("""\b\d+(\.\d+)?\b""")
            numberPattern.findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = colorNumber),
                    match.range.first,
                    match.range.last + 1
                )
            }

            // 4. Preprocessor for C/C++
            if (language == Language.C || language == Language.CPP) {
                Regex("""#\s*\w+""").findAll(text).forEach { match ->
                    addStyle(
                        SpanStyle(color = colorPreprocessor, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // 5. HTML tags
            if (language == Language.HTML) {
                Regex("""</?[a-zA-Z0-9]+(\s|/|>|$)""").findAll(text).forEach { match ->
                    addStyle(
                        SpanStyle(color = colorHtmlTag, fontWeight = FontWeight.SemiBold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // 6. Keywords
            val keywords = when (language) {
                Language.PYTHON -> pythonKeywords
                Language.C, Language.CPP -> cCppKeywords
                Language.JAVA -> javaKeywords
                Language.JAVASCRIPT -> jsKeywords
                Language.CSS -> setOf("color", "background", "margin", "padding", "border", "font-family", "display", "flex", "width", "height")
                Language.HTML -> setOf("DOCTYPE", "html", "head", "body", "title", "meta", "style", "script")
            }

            val wordPattern = Regex("""\b[a-zA-Z_][a-zA-Z0-9_]*\b""")
            wordPattern.findAll(text).forEach { match ->
                val word = match.value
                if (keywords.contains(word)) {
                    addStyle(
                        SpanStyle(color = colorKeyword, fontWeight = FontWeight.SemiBold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }
        }
    }
}
