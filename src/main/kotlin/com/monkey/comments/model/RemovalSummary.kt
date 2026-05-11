package com.monkey.comments.model

data class RemovalSummary(
    val removedComments: Int,
    val processedFiles: Int,
) {
    companion object {
        val EMPTY = RemovalSummary(removedComments = 0, processedFiles = 0)
    }
}
