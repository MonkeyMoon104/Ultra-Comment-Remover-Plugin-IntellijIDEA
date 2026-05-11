package com.monkey.comments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.monkey.comments.model.CommentRemovalPlan

class CommentRemovalWriter(project: Project) {
    private val documentManager = PsiDocumentManager.getInstance(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val writeCommand = WriteCommandAction.writeCommandAction(project)
        .withName("Remove Comments")

    fun applyPlan(plan: CommentRemovalPlan) {
        ApplicationManager.getApplication().invokeAndWait {
            writeCommand.run<RuntimeException> {
                val document = fileDocumentManager.getDocument(plan.virtualFile) ?: return@run
                plan.edits.forEach { edit ->
                    document.replaceString(edit.range.startOffset, edit.range.endOffset, edit.replacement)
                }
                documentManager.commitDocument(document)
                fileDocumentManager.saveDocument(document)
            }
        }
    }
}
