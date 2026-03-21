package com.github.leomillon.uuidgenerator.settings.uuid.form

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import java.util.UUID
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class UUIDGeneratorSettingsForm {

    private val uuidV4Preview = UUID.fromString("242e6506-710e-4af9-8b3d-dfcfad4bc9a6")
    private val uuidV7Preview = UUID.fromString("019d10e9-3e28-7683-9758-8d7a97205099")

    private var panel: JPanel? = null
    private var previewValue: JLabel? = null
    private var uuidVersion4RadioButton: JRadioButton? = null
    private var uuidVersion7RadioButton: JRadioButton? = null
    private var lowerCaseRadioButton: JRadioButton? = null
    private var upperCaseRadioButton: JRadioButton? = null
    private var withDashesRadioButton: JRadioButton? = null
    private var withoutDashesRadioButton: JRadioButton? = null
    private var longSizeRadioButton: JRadioButton? = null
    private var shortSizeRadioButton: JRadioButton? = null
    private var highlightingCheckbox: JCheckBox? = null

    private val settings: UUIDGeneratorSettings = UUIDGeneratorSettings.instance

    init {
        loadSettings()
    }

    fun loadSettings() {
        uuidVersion4RadioButton?.isSelected = settings.uuidVersion4
        uuidVersion7RadioButton?.isSelected = !settings.uuidVersion4
        lowerCaseRadioButton?.isSelected = settings.lowerCased
        upperCaseRadioButton?.isSelected = !settings.lowerCased
        withDashesRadioButton?.isSelected = settings.withDashes
        withoutDashesRadioButton?.isSelected = !settings.withDashes
        longSizeRadioButton?.isSelected = settings.longSize
        shortSizeRadioButton?.isSelected = !settings.longSize
        highlightingCheckbox?.isSelected = settings.codeHighlighting

        sequenceOf(
            uuidVersion4RadioButton,
            uuidVersion7RadioButton,
            lowerCaseRadioButton,
            upperCaseRadioButton,
            withDashesRadioButton,
            withoutDashesRadioButton,
            longSizeRadioButton,
            shortSizeRadioButton
        )
            .filterNotNull()
            .forEach { it.addItemListener { updatePreview() } }

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
    }

    fun component(): JComponent? = panel

    private fun isUuidVersion4() = uuidVersion4RadioButton?.isSelected
    private fun isLowerCased() = lowerCaseRadioButton?.isSelected
    private fun isWithDashes() = withDashesRadioButton?.isSelected
    private fun isLongSize() = longSizeRadioButton?.isSelected
    private fun codeHighlightingEnabled() = highlightingCheckbox?.isSelected

    val isModified: Boolean
        get() = (isUuidVersion4() != settings.uuidVersion4
            || isLowerCased() != settings.lowerCased
            || isWithDashes() != settings.withDashes
            || isLongSize() != settings.longSize
            || codeHighlightingEnabled() != settings.codeHighlighting)
}
