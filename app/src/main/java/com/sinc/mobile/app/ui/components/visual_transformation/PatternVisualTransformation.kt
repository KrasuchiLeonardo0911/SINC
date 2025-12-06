package com.sinc.mobile.app.ui.components.visual_transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PatternVisualTransformation(private val pattern: String) : VisualTransformation {

    private val maskChar = '#'
    private val maxLength = pattern.count { it == maskChar }

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= maxLength) text.text.substring(0, maxLength) else text.text

        val annotatedString = AnnotatedString.Builder().apply {
            var textIndex = 0
            var patternIndex = 0
            while (textIndex < trimmed.length && patternIndex < pattern.length) {
                if (pattern[patternIndex] == maskChar) {
                    append(trimmed[textIndex])
                    textIndex++
                } else {
                    append(pattern[patternIndex])
                }
                patternIndex++
            }
        }.toAnnotatedString()

        return TransformedText(annotatedString, object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var noneDigitCount = 0
                var i = 0
                while (i < offset + noneDigitCount) {
                    if (i < pattern.length && pattern[i] != '#') {
                        noneDigitCount++
                    }
                    if (i >= pattern.length) break // safety break
                    i++
                }
                return (offset + noneDigitCount).coerceAtMost(annotatedString.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                var noneDigitCount = 0
                for (i in 0 until offset) {
                    if (i < pattern.length && pattern[i] != '#') {
                        noneDigitCount++
                    }
                }
                return (offset - noneDigitCount).coerceAtMost(trimmed.length)
            }
        })
    }
}
