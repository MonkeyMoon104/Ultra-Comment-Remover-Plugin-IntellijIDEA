package com.monkey.comments.model

import com.intellij.openapi.util.TextRange

data class CommentEdit(
    val range: TextRange,
    val replacement: String,
)
