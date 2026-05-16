package com.github.leomillon.uuidgenerator.settings.uuid.form

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.settings.UUIDGeneratorBaseForm
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import com.github.lgooddatepicker.components.DateTimePicker
import java.awt.ItemSelectable
import java.util.*
import javax.swing.*

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
    override var timePicker: DateTimePicker? = null

    private val uuidV4Preview = UUID.fromString("242e6506-710e-4af9-8b3d-dfcfad4bc9a6")
    private val uuidV7Preview = UUID.fromString("019d10e9-3e28-7683-9758-8d7a97205099")

    private var previewValue: JLabel? = null
    private var highlightingCheckbox: JCheckBox? = null
    private var debuggerInsightCheckbox: JCheckBox? = null

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
        debuggerInsightCheckbox?.isSelected = settings.debuggerInsight
        currentTimeRadioButton?.isSelected = !settings.fixedTime
        fixedTimeRadioButton?.isSelected = settings.fixedTime

        setPanelEnabled(timePanel, settings.fixedTime && !settings.uuidVersion4)

        timePicker?.datePicker?.settings?.setFormatForDatesCommonEra("yyyy-MM-dd")
        timePicker?.dateTimePermissive = settings.uuidCreationTime

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
        settings.debuggerInsight = debuggerInsightEnabled() ?: true
        settings.fixedTime = isFixedTime()
        settings.uuidCreationTime = uuidCreationTime()
    }

    fun component(): JComponent? = panel

    private fun codeHighlightingEnabled() = highlightingCheckbox?.isSelected
    private fun debuggerInsightEnabled() = debuggerInsightCheckbox?.isSelected

    val isModified: Boolean
        get() = (isUuidVersion4() != settings.uuidVersion4
                || isLowerCased() != settings.lowerCased
                || isWithDashes() != settings.withDashes
                || isLongSize() != settings.longSize
                || codeHighlightingEnabled() != settings.codeHighlighting
                || debuggerInsightEnabled() != settings.debuggerInsight
                || isFixedTime() != settings.fixedTime
                || uuidCreationTime() != settings.uuidCreationTime
                )
}
