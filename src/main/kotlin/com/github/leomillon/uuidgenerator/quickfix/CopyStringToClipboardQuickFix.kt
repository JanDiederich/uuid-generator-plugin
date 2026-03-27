package com.github.leomillon.uuidgenerator.quickfix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.ui.TextTransferable

class CopyStringToClipboardQuickFix(
    private val actionName: String,
    private val source: String
) : BaseIntentionAction(), HighPriorityAction {

    override fun getFamilyName(): String = "Clipboard"

    override fun getText(): String = actionName

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile?): Boolean = true

    override fun invoke(project: Project, editor: Editor, file: PsiFile?) {
        CopyPasteManager.getInstance().setContents(TextTransferable(source as CharSequence))
    }

    override fun generatePreview(
        project: Project,
        editor: Editor,
        psiFile: PsiFile
    ): IntentionPreviewInfo {
        return IntentionPreviewInfo.Html("<p>${source}</p>")
    }
}