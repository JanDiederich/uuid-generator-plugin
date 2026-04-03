package com.github.leomillon.uuidgenerator.popup.uuid.examiner

import com.github.leomillon.uuidgenerator.DisplayMessageUtils
import com.github.leomillon.uuidgenerator.popup.GeneratePopup
import com.github.leomillon.uuidgenerator.popup.uuid.UUIDExaminerPopupForm
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.util.ui.TextTransferable
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

class ExamineUUIDPopup : GeneratePopup() {

    var popupForm: UUIDExaminerPopupForm? = null

    init {
        init()
        title = "UUID examiner Popup"
    }

    override fun createActions(): Array<Action> {
        val actions: Array<Action> = super.createActions()

        val copyHtmlToClipboard = object : AbstractAction("Copy HTML to clipboard") {
            override fun actionPerformed(e: ActionEvent?) {
                val html = popupForm?.buildHtmlOutput()
                if (html != null) {
                    CopyPasteManager.getInstance().setContents(TextTransferable(html as CharSequence))

                    val project: Project? = DataManager.getInstance() //
                        .getDataContext(popupForm?.component()) //
                        .getData(CommonDataKeys.PROJECT)
                    project?.run {
                        DisplayMessageUtils.displayMessage(
                            "Copied HTML to clipboard", this, fadeoutTime = 2000
                        )
                    }
                }/* If the standard OK_EXIT_CODE code is used, the standard action,
                with the Markdown copy-to-clipboard action is called, and instantly
                overwrites the HTML clipboard content with the Markdown content. */
                close(NEXT_USER_EXIT_CODE)
            }
        }
        return (listOf<Action>(copyHtmlToClipboard) + actions).toTypedArray()
    }

    override fun createCenterPanel(): JComponent? {
        popupForm = popupForm ?: UUIDExaminerPopupForm()
        return popupForm?.component()
    }

    override fun getOutput() = popupForm?.buildMarkdownOutput()

    override fun saveSettings() {
        popupForm?.applyToSettings(UUIDExaminerPopupSettings.instance)
    }

    override fun actionName(): String = "Copy Markdown table to clipboard"
}
