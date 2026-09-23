package com.github.leomillon.uuidgenerator.popup.uuid.generator

import com.github.leomillon.uuidgenerator.popup.GeneratePopup
import javax.swing.JComponent

class GenerateUUIDPopup : GeneratePopup() {

    var popupForm: UUIDGeneratorPopupForm? = null

    init {
        init()
        title = "UUID V4 & V7 Generator Popup"
    }

    override fun createCenterPanel(): JComponent? {
        popupForm = popupForm ?: UUIDGeneratorPopupForm()
        return popupForm?.component()
    }

    override fun getOutput() = popupForm?.getOutput()

    override fun saveSettings() {
        popupForm?.applyToSettings(UUIDGeneratorPopupSettings.instance)
    }
}
