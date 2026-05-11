package com.monkey.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.monkey.comments.CommentRemovalCoordinator
import com.monkey.comments.CommentRemovalPlanner
import com.monkey.comments.CommentRemovalWriter
import com.monkey.comments.model.RemovalSummary
import com.monkey.notifications.NotificationHelper

class RemoveCommentsAction : DumbAwareAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val targets = ProjectViewTargetResolver.resolve(event)
        val visible = project != null && targets.isNotEmpty()

        event.presentation.isVisible = visible
        event.presentation.isEnabled = visible
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val targets = ProjectViewTargetResolver.resolve(event).toTypedArray()
        if (targets.isEmpty()) {
            return
        }

        val summary = runRemoval(project, targets)
        NotificationHelper.notify(
            project = project,
            title = "Comment removal completed",
            content = "Removed ${summary.removedComments} comments from ${summary.processedFiles} files.",
            type = NotificationType.INFORMATION,
        )
    }

    private fun runRemoval(project: Project, targets: Array<out VirtualFile>): RemovalSummary {
        var summary = RemovalSummary.EMPTY
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                val indicator = ProgressManager.getInstance().progressIndicator ?: ProgressIndicatorBase()
                summary = createCoordinator(project).removeComments(targets.toList(), indicator)
            },
            "Remove Comments",
            false,
            project,
        )
        return summary
    }

    private fun createCoordinator(project: Project): CommentRemovalCoordinator {
        return CommentRemovalCoordinator(
            planner = CommentRemovalPlanner(project),
            writer = CommentRemovalWriter(project),
        )
    }
}
