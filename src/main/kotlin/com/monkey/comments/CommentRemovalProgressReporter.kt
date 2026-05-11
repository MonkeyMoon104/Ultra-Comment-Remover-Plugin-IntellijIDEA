package com.monkey.comments

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile

class CommentRemovalProgressReporter {

    fun onCollectionStarted(indicator: ProgressIndicator?) {
        indicator ?: return
        indicator.isIndeterminate = true
        indicator.text = "Collecting source files"
        indicator.text2 = "Scanning selected files and directories"
    }

    fun onPlanningStarted(indicator: ProgressIndicator?) {
        indicator ?: return
        indicator.isIndeterminate = false
        indicator.fraction = 0.0
        indicator.text = "Removing comments"
        indicator.text2 = "Analyzing PSI trees"
    }

    fun onPlanningFile(indicator: ProgressIndicator?, index: Int, total: Int, virtualFile: VirtualFile) {
        indicator ?: return
        indicator.text = "Removing comments"
        indicator.text2 = "Analyzing ${virtualFile.name}"
        indicator.fraction = computeFraction(index, total, 0.0, 0.55)
    }

    fun onWritingFile(indicator: ProgressIndicator?, index: Int, total: Int, virtualFile: VirtualFile) {
        indicator ?: return
        indicator.text = "Removing comments"
        indicator.text2 = "Updating ${virtualFile.name}"
        indicator.fraction = computeFraction(index, total, 0.55, 1.0)
    }

    fun onCompleted(indicator: ProgressIndicator?) {
        indicator ?: return
        indicator.fraction = 1.0
        indicator.text2 = "Completed"
    }

    private fun computeFraction(index: Int, total: Int, start: Double, end: Double): Double {
        if (total <= 0) {
            return end
        }

        val progress = index.toDouble() / total.toDouble()
        return start + ((end - start) * progress)
    }
}
