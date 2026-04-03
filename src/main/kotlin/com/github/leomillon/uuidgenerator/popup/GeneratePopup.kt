package com.github.leomillon.uuidgenerator.popup

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.AbstractAction
import javax.swing.Action

abstract class GeneratePopup : DialogWrapper(true), IGeneratePopup {

    override fun createActions(): Array<Action> {
        val actions = super.createActions()
        okAction.putValue(Action.NAME, actionName())
        return actions
    }

    open fun actionName(): String = "Copy to clipboard"
}
