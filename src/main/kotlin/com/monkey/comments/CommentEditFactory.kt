package com.monkey.comments

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.monkey.comments.model.CommentEdit

class CommentEditFactory {

    fun create(comment: PsiComment, document: Document): CommentEdit? {
        if (!comment.isValid) {
            return null
        }

        val text = document.charsSequence
        val range = comment.textRange

        if (isStandaloneCommentBlock(range, text, document)) {
            return createStandaloneLineEdit(range, document)
        }

        if (isTrailingLineComment(comment, range, text, document)) {
            return createTrailingLineCommentEdit(range, text)
        }

        return createInlineCommentEdit(range, text)
    }

    private fun isStandaloneCommentBlock(range: TextRange, text: CharSequence, document: Document): Boolean {
        val startLine = document.getLineNumber(range.startOffset)
        val safeEndOffset = (range.endOffset - 1).coerceAtLeast(range.startOffset).coerceAtMost(document.textLength - 1)
        val endLine = document.getLineNumber(safeEndOffset)

        val lineStart = document.getLineStartOffset(startLine)
        val lineEnd = document.getLineEndOffset(endLine)
        return text.subSequence(lineStart, range.startOffset).isBlank() &&
            text.subSequence(range.endOffset.coerceAtMost(document.textLength), lineEnd).isBlank()
    }

    private fun createStandaloneLineEdit(range: TextRange, document: Document): CommentEdit {
        val startLine = document.getLineNumber(range.startOffset)
        val safeEndOffset = (range.endOffset - 1).coerceAtLeast(range.startOffset).coerceAtMost(document.textLength - 1)
        val endLine = document.getLineNumber(safeEndOffset)
        val deleteStartLine = if (startLine > 0 && isBlankLine(document, startLine - 1)) startLine - 1 else startLine
        val deleteEndLine = if (deleteStartLine == startLine && endLine + 1 < document.lineCount && isBlankLine(document, endLine + 1)) {
            endLine + 1
        } else {
            endLine
        }

        val lineStart = document.getLineStartOffset(deleteStartLine)
        val lineEnd = document.getLineEndOffset(deleteEndLine)
        val deleteEnd = when {
            lineEnd < document.textLength && document.charsSequence[lineEnd] == '\r' ->
                if (lineEnd + 1 < document.textLength && document.charsSequence[lineEnd + 1] == '\n') {
                    lineEnd + 2
                } else {
                    lineEnd + 1
                }

            lineEnd < document.textLength && document.charsSequence[lineEnd] == '\n' -> lineEnd + 1
            else -> lineEnd
        }

        return CommentEdit(TextRange(lineStart, deleteEnd), "")
    }

    private fun isBlankLine(document: Document, line: Int): Boolean {
        if (line < 0 || line >= document.lineCount) {
            return false
        }

        val start = document.getLineStartOffset(line)
        val end = document.getLineEndOffset(line)
        return document.charsSequence.subSequence(start, end).isBlank()
    }

    private fun isTrailingLineComment(
        comment: PsiComment,
        range: TextRange,
        text: CharSequence,
        document: Document,
    ): Boolean {
        if (comment.text.contains('\n') || comment.text.contains('\r')) {
            return false
        }

        val lineEnd = document.getLineEndOffset(document.getLineNumber(range.startOffset))
        return text.subSequence(range.endOffset, lineEnd).isBlank()
    }

    private fun createTrailingLineCommentEdit(range: TextRange, text: CharSequence): CommentEdit {
        var startOffset = range.startOffset
        while (startOffset > 0 && isHorizontalWhitespace(text[startOffset - 1])) {
            startOffset--
        }
        return CommentEdit(TextRange(startOffset, range.endOffset), "")
    }

    private fun createInlineCommentEdit(range: TextRange, text: CharSequence): CommentEdit {
        var endOffset = range.endOffset
        val previousChar = charBefore(text, range.startOffset)
        val nextChar = charAfter(text, endOffset)

        if (isHorizontalWhitespace(previousChar) && isHorizontalWhitespace(nextChar)) {
            while (endOffset < text.length && isHorizontalWhitespace(text[endOffset])) {
                endOffset++
            }
            return CommentEdit(TextRange(range.startOffset, endOffset), "")
        }

        val replacement = if (needsSeparator(previousChar, nextChar)) " " else ""
        return CommentEdit(TextRange(range.startOffset, endOffset), replacement)
    }

    private fun needsSeparator(previousChar: Char?, nextChar: Char?): Boolean {
        if (previousChar == null || nextChar == null) {
            return false
        }
        if (previousChar.isWhitespace() || nextChar.isWhitespace()) {
            return false
        }

        return previousChar.isTokenCharacter() && nextChar.isTokenCharacter()
    }

    private fun charBefore(text: CharSequence, startOffset: Int): Char? =
        if (startOffset > 0) text[startOffset - 1] else null

    private fun charAfter(text: CharSequence, endOffset: Int): Char? =
        if (endOffset < text.length) text[endOffset] else null

    private fun isHorizontalWhitespace(value: Char?): Boolean = value == ' ' || value == '\t'

    private fun Char.isTokenCharacter(): Boolean = isLetterOrDigit() || this == '_' || this == '$'
}
