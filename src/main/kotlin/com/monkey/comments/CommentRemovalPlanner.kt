package com.monkey.comments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.monkey.comments.model.CommentEdit
import com.monkey.comments.model.CommentRemovalPlan

class CommentRemovalPlanner(
    project: Project,
    private val preservationPolicy: CommentPreservationPolicy = CommentPreservationPolicy(),
) {
    private val psiManager = PsiManager.getInstance(project)
    private val documentManager = PsiDocumentManager.getInstance(project)
    private val editFactory = CommentEditFactory()

    fun buildPlans(
        files: List<VirtualFile>,
        onFileVisited: (index: Int, total: Int, file: VirtualFile) -> Unit = { _, _, _ -> },
    ): List<CommentRemovalPlan> {
        commitAllDocuments()

        return ReadAction.compute<List<CommentRemovalPlan>, RuntimeException> {
            files.mapIndexedNotNull { index, virtualFile ->
                onFileVisited(index, files.size, virtualFile)

                val psiFile = psiManager.findFile(virtualFile) ?: return@mapIndexedNotNull null
                val document = documentManager.getDocument(psiFile) ?: return@mapIndexedNotNull null
                buildPlan(virtualFile, psiFile, document)
            }
        }
    }

    private fun commitAllDocuments() {
        ApplicationManager.getApplication().invokeAndWait {
            documentManager.commitAllDocuments()
        }
    }

    private fun buildPlan(virtualFile: VirtualFile, psiFile: PsiFile, document: Document): CommentRemovalPlan? {
        val edits = collectEdits(psiFile, document)
        if (edits.isEmpty()) {
            return null
        }

        return CommentRemovalPlan(
            virtualFile = virtualFile,
            edits = edits.sortedByDescending { it.range.startOffset },
        )
    }

    private fun collectEdits(psiFile: PsiFile, document: Document): List<CommentEdit> {
        val edits = mutableListOf<CommentEdit>()

        psiFile.accept(
            object : PsiRecursiveElementWalkingVisitor() {
                override fun visitComment(comment: PsiComment) {
                    if (preservationPolicy.shouldPreserveComment(comment.text)) {
                        return
                    }

                    editFactory.create(comment, document)?.let(edits::add)
                }
            },
        )

        return edits
    }
}
