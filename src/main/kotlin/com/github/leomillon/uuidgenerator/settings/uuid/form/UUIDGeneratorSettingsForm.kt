package com.github.leomillon.uuidgenerator.settings.uuid.form

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.settings.UUIDGeneratorBaseForm
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import java.awt.ItemSelectable
import java.util.*
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner

class UUIDGeneratorSettingsForm : UUIDGeneratorBaseForm {

    override var panel: JPanel? = null

    override var uuidV4RadioButton: JRadioButton? = null
    override var uuidV7RadioButton: JRadioButton? = null
    override var lowerCaseRadioButton: JRadioButton? = null
    override var upperCaseRadioButton: JRadioButton? = null
    override var withDashesRadioButton: JRadioButton? = null
    override var withoutDashesRadioButton: JRadioButton? = null
    override var longSizeRadioButton: JRadioButton? = null
    override var shortSizeRadioButton: JRadioButton? = null

    override var currentTimeRadioButton: JRadioButton? = null
    override var fixedTimeRadioButton: JRadioButton? = null
    override var timePanel: JPanel? = null
    override var yearSpinner: JSpinner? = null
    override var monthSpinner: JSpinner? = null
    override var daySpinner: JSpinner? = null
    override var hourSpinner: JSpinner? = null
    override var minuteSpinner: JSpinner? = null
    override var secondSpinner: JSpinner? = null
    override var millisSpinner: JSpinner? = null

    private val uuidV4Preview = UUID.fromString("242e6506-710e-4af9-8b3d-dfcfad4bc9a6")
    private val uuidV7Preview = UUID.fromString("019d10e9-3e28-7683-9758-8d7a97205099")

    private var previewValue: JLabel? = null
    private var highlightingCheckbox: JCheckBox? = null

    private val settings: UUIDGeneratorSettings = UUIDGeneratorSettings.instance

    init {
        loadSettings()
    }

    fun loadSettings() {
        uuidV4RadioButton?.isSelected = settings.uuidVersion4
        uuidV7RadioButton?.isSelected = !settings.uuidVersion4
        lowerCaseRadioButton?.isSelected = settings.lowerCased
        upperCaseRadioButton?.isSelected = !settings.lowerCased
        withDashesRadioButton?.isSelected = settings.withDashes
        withoutDashesRadioButton?.isSelected = !settings.withDashes
        longSizeRadioButton?.isSelected = settings.longSize
        shortSizeRadioButton?.isSelected = !settings.longSize
        highlightingCheckbox?.isSelected = settings.codeHighlighting
        currentTimeRadioButton?.isSelected = !settings.fixedTime
        fixedTimeRadioButton?.isSelected = settings.fixedTime

        setPanelEnabled(timePanel, settings.fixedTime && !settings.uuidVersion4)

        yearSpinner?.value = settings.year
        monthSpinner?.value = settings.month
        daySpinner?.value = settings.day
        hourSpinner?.value = settings.hour
        minuteSpinner?.value = settings.minute
        secondSpinner?.value = settings.second
        millisSpinner?.value = settings.millis

        sequenceOf<ItemSelectable?>(
            uuidV4RadioButton,
            uuidV7RadioButton,
            lowerCaseRadioButton,
            upperCaseRadioButton,
            withDashesRadioButton,
            withoutDashesRadioButton,
            longSizeRadioButton,
            shortSizeRadioButton,
            currentTimeRadioButton,
            fixedTimeRadioButton
        )
            .filterNotNull()
            .forEach { uiComponent: ItemSelectable? ->
                uiComponent?.addItemListener {
                    if (isUuid7TimeComponent(uiComponent)) {
                        setPanelEnabled(timePanel, isFixedTime() && isUuidVersion7())
                    }
                    updatePreview()
                }
            }

        updatePreview()
    }

    private fun updatePreview() {
        val previewSettings =
            UUIDGeneratorSettings()
        applyToSettings(previewSettings)
        previewValue?.text =
            UUIDGenerator.formatUUID(
                if (previewSettings.uuidVersion4) uuidV4Preview else uuidV7Preview,
                previewSettings
            )
    }

    fun applyToSettings(settings: UUIDGeneratorSettings) {
        settings.uuidVersion4 = isUuidVersion4() ?: true
        settings.lowerCased = isLowerCased() ?: true
        settings.withDashes = isWithDashes() ?: true
        settings.longSize = isLongSize() ?: true
        settings.codeHighlighting = codeHighlightingEnabled() ?: true
        settings.fixedTime = isFixedTime()

        settings.year = year()
        settings.month = month()
        settings.day = dayOfMonth()
        settings.hour = hour()
        settings.minute = minute()
        settings.second = second()
        settings.millis = millis()
    }

    fun component(): JComponent? = panel

    private fun codeHighlightingEnabled() = highlightingCheckbox?.isSelected

    val isModified: Boolean
        get() = (isUuidVersion4() != settings.uuidVersion4
                || isLowerCased() != settings.lowerCased
                || isWithDashes() != settings.withDashes
                || isLongSize() != settings.longSize
                || codeHighlightingEnabled() != settings.codeHighlighting
                || isFixedTime() != settings.fixedTime

                || year() != settings.year
                || month() != settings.month
                || dayOfMonth() != settings.day
                || hour() != settings.hour
                || minute() != settings.minute
                || second() != settings.second
                || millis() != settings.millis
                )
}
