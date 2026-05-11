package com.monkey.notifications

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationHelper {

    private const val GROUP_ID = "UltraCommentRemover"

    fun notify(project: Project?, title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, content, type)
            .notify(project)
    }
}
