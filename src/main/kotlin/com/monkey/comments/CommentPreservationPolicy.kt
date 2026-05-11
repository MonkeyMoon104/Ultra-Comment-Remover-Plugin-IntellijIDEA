package com.monkey.comments

import java.util.Locale

class CommentPreservationPolicy {

    fun shouldPreserveComment(commentText: String): Boolean {
        val text = commentText.trim()
        val normalized = normalizeCommentText(text)

        if (text.startsWith("#!")) {
            return true
        }
        if (normalized.startsWith("noinspection")) {
            return true
        }
        if (normalized.contains("formatter:off") || normalized.contains("formatter:on")) {
            return true
        }
        if (normalized.contains("eslint-disable") || normalized.contains("eslint-enable")) {
            return true
        }
        if (normalized.contains("tslint:disable") || normalized.contains("tslint:enable")) {
            return true
        }
        if (normalized.startsWith("region") || normalized.startsWith("endregion")) {
            return true
        }
        if (normalized.contains("copyright") || normalized.contains("licensed under")) {
            return true
        }

        return false
    }

    private fun normalizeCommentText(text: String): String {
        return text
            .removePrefix("/*")
            .removeSuffix("*/")
            .removePrefix("//")
            .removePrefix("#")
            .removePrefix("--")
            .trim()
            .lowercase(Locale.ROOT)
    }
}
