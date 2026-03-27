package com.github.leomillon.uuidgenerator.settings

import com.github.leomillon.uuidgenerator.FixedTimeGenerating
import kotlinx.datetime.number
import java.awt.Component
import java.awt.ItemSelectable
import java.time.OffsetDateTime
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

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
    var yearSpinner: JSpinner?
    var monthSpinner: JSpinner?
    var daySpinner: JSpinner?
    var hourSpinner: JSpinner?
    var minuteSpinner: JSpinner?
    var secondSpinner: JSpinner?
    var millisSpinner: JSpinner?

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
    override fun year(): Int = (yearSpinner?.value ?: OffsetDateTime.now().year) as Int
    override fun month(): Int = (monthSpinner?.value ?: OffsetDateTime.now().month) as Int
    override fun dayOfMonth(): Int = (daySpinner?.value ?: OffsetDateTime.now().dayOfMonth) as Int
    override fun hour(): Int = (hourSpinner?.value ?: OffsetDateTime.now().hour) as Int
    override fun minute(): Int = (minuteSpinner?.value ?: OffsetDateTime.now().minute) as Int
    override fun second(): Int = (secondSpinner?.value ?: OffsetDateTime.now().second) as Int
    override fun millis(): Int = (millisSpinner?.value ?: (OffsetDateTime.now().nano / 1_000_000)) as Int

    // Called by the IntelliJ Swing designer.
    fun createUIComponents() {
        yearSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().year, -9999, 9999, 1))
        monthSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().month.number, 1, 12, 1))
        daySpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().dayOfMonth, 1, 31, 1))
        hourSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().hour, 0, 23, 1))
        minuteSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().minute, 0, 59, 1))
        secondSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().second, 0, 59, 1))
        millisSpinner = JSpinner(SpinnerNumberModel(OffsetDateTime.now().nano / 1_000_000, 0, 999, 1))
    }
}