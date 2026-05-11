package com.monkey.comments.model

import com.intellij.openapi.vfs.VirtualFile

data class CommentRemovalPlan(
    val virtualFile: VirtualFile,
    val edits: List<CommentEdit>,
)
