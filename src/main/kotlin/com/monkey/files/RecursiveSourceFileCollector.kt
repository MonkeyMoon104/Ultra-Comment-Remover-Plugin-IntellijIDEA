package com.monkey.files

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.util.LinkedHashSet

object RecursiveSourceFileCollector {

    private val excludedDirectories = setOf(".git", "build", "target", "node_modules", ".idea", "out", "dist")

    fun collect(targets: List<VirtualFile>): List<VirtualFile> {
        val result = LinkedHashSet<VirtualFile>()

        targets.forEach { target ->
            collectFromTarget(target, result)
        }

        return result.toList()
    }

    private fun collectFromTarget(
        target: VirtualFile,
        result: MutableSet<VirtualFile>,
    ) {
        if (target.isDirectory) {
            VfsUtilCore.iterateChildrenRecursively(
                target,
                { fileOrDirectory ->
                    if (fileOrDirectory.isDirectory) {
                        !isExcludedDirectory(fileOrDirectory)
                    } else {
                        true
                    }
                },
                { file ->
                    if (!file.isDirectory && !file.fileType.isBinary && !isUnderExcludedDirectory(file)) {
                        result.add(file)
                    }
                    true
                },
            )
            return
        }

        if (!target.fileType.isBinary && !isUnderExcludedDirectory(target)) {
            result.add(target)
        }
    }

    private fun isExcludedDirectory(file: VirtualFile): Boolean = file.name in excludedDirectories

    private fun isUnderExcludedDirectory(file: VirtualFile): Boolean {
        var current: VirtualFile? = file.parent
        while (current != null) {
            if (isExcludedDirectory(current)) {
                return true
            }
            current = current.parent
        }
        return false
    }
}
