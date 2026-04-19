package com.github.leomillon.uuidgenerator.settings

import com.github.leomillon.uuidgenerator.FixedTimeGenerating
import com.github.lgooddatepicker.components.DateTimePicker
import java.awt.Component
import java.awt.ItemSelectable
import java.time.LocalDateTime
import javax.swing.JPanel
import javax.swing.JRadioButton

interface UUIDGeneratorBaseForm : FixedTimeGenerating {
    var panel: JPanel?

    var uuidV4RadioButton: JRadioButton?
    var uuidV7RadioButton: JRadioButton?
    var lowerCaseRadioButton: JRadioButton?
    var upperCaseRadioButton: JRadioButton?
    var withDashesRadioButton: JRadioButton?
    var withoutDashesRadioButton: JRadioButton?
    var longSizeRadioButton: JRadioButton?
    var shortSizeRadioButton: JRadioButton?

    var currentTimeRadioButton: JRadioButton?
    var fixedTimeRadioButton: JRadioButton?
    var timePanel: JPanel?
    var timePicker: DateTimePicker?

    fun isUuidVersion4() = uuidV4RadioButton?.isSelected
    fun isUuidVersion7() = uuidV7RadioButton?.isSelected ?: false
    fun isLowerCased() = lowerCaseRadioButton?.isSelected
    fun isWithDashes() = withDashesRadioButton?.isSelected
    fun isLongSize() = longSizeRadioButton?.isSelected
    fun isVersion4() = uuidV4RadioButton?.isSelected
    fun isVersion7() = uuidV7RadioButton?.isSelected

    fun isUuid7TimeComponent(uiComponent: ItemSelectable): Boolean = (uiComponent == uuidV4RadioButton
            || uiComponent == uuidV7RadioButton
            || uiComponent == currentTimeRadioButton
            || uiComponent == fixedTimeRadioButton)

    fun setPanelEnabled(panel: JPanel?, isEnabled: Boolean) {
        if (panel == null) {
            return
        }
        panel.setEnabled(isEnabled)
        val components: Array<Component> = panel.components
        for (component in components) {
            if (component is JPanel) {
                setPanelEnabled((component as JPanel?), isEnabled)
            }
            component.isEnabled = isEnabled
        }
    }

    fun isFixedTime(): Boolean = fixedTimeRadioButton?.isSelected ?: false
    override fun uuidCreationTime(): LocalDateTime? = timePicker?.dateTimePermissive
}