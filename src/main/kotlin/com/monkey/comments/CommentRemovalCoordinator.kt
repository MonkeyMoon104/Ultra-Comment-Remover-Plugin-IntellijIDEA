package com.monkey.comments

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vfs.VirtualFile
import com.monkey.comments.model.CommentRemovalPlan
import com.monkey.comments.model.RemovalSummary
import com.monkey.files.RecursiveSourceFileCollector

class CommentRemovalCoordinator(
    private val planner: CommentRemovalPlanner,
    private val writer: CommentRemovalWriter,
    private val progressReporter: CommentRemovalProgressReporter = CommentRemovalProgressReporter(),
) {

    fun removeComments(targets: List<VirtualFile>, indicator: ProgressIndicator?): RemovalSummary {
        progressReporter.onCollectionStarted(indicator)
        val files = RecursiveSourceFileCollector.collect(targets)
        if (files.isEmpty()) {
            progressReporter.onCompleted(indicator)
            return RemovalSummary.EMPTY
        }

        progressReporter.onPlanningStarted(indicator)
        val plans = planner.buildPlans(files) { index, total, file ->
            progressReporter.onPlanningFile(indicator, index, total, file)
        }

        if (plans.isEmpty()) {
            progressReporter.onCompleted(indicator)
            return RemovalSummary.EMPTY
        }

        applyPlans(plans, indicator)
        progressReporter.onCompleted(indicator)
        return RemovalSummary(
            removedComments = plans.sumOf { it.edits.size },
            processedFiles = plans.size,
        )
    }

    private fun applyPlans(plans: List<CommentRemovalPlan>, indicator: ProgressIndicator?) {
        plans.forEachIndexed { index, plan ->
            progressReporter.onWritingFile(indicator, index, plans.size, plan.virtualFile)
            writer.applyPlan(plan)
        }
    }
}
