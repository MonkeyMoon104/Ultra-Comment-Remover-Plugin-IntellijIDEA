package com.monkey.actions

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

object ProjectViewTargetResolver {

    fun resolve(event: AnActionEvent): List<VirtualFile> {
        val directTargets = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList().orEmpty()
        if (directTargets.isNotEmpty()) {
            return directTargets
        }

        event.getData(CommonDataKeys.VIRTUAL_FILE)?.let { return listOf(it) }

        val project = event.project ?: return emptyList()
        if (event.place == ActionPlaces.PROJECT_VIEW_POPUP) {
            project.basePath
                ?.let(LocalFileSystem.getInstance()::findFileByPath)
                ?.let { return listOf(it) }
        }

        return emptyList()
    }
}
