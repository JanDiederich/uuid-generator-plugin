package com.github.leomillon.uuidgenerator.popup.uuid

import com.github.f4b6a3.uuid.UuidCreator
import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.settings.UUIDGeneratorBaseForm
import java.awt.ItemSelectable
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.*

class UUIDGeneratorPopupForm : UUIDGeneratorBaseForm {

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

    private var numberInputField: JSpinner? = null
    private var separatorInputField: JTextField? = null
    private var prefixInputField: JTextField? = null
    private var suffixInputField: JTextField? = null
    private var resultOutputField: JTextArea? = null

    private var currentIds = listOf<UUID>()

    private val settings: UUIDGeneratorPopupSettings = UUIDGeneratorPopupSettings.instance

    init {
        loadSettings()
    }

    private fun loadSettings() {
        uuidV4RadioButton?.isSelected = settings.version4
        uuidV7RadioButton?.isSelected = !settings.version4
        lowerCaseRadioButton?.isSelected = settings.lowerCased
        upperCaseRadioButton?.isSelected = !settings.lowerCased
        withDashesRadioButton?.isSelected = settings.withDashes
        withoutDashesRadioButton?.isSelected = !settings.withDashes
        longSizeRadioButton?.isSelected = settings.longSize
        shortSizeRadioButton?.isSelected = !settings.longSize
        numberInputField?.model = SpinnerNumberModel(settings.numberFieldValue, 1, Int.MAX_VALUE, 1)
        separatorInputField?.text = settings.separatorFieldValue
        prefixInputField?.text = settings.prefixFieldValue
        suffixInputField?.text = settings.suffixFieldValue
        resultOutputField?.text = ""

        currentTimeRadioButton?.isSelected = !settings.fixedTime
        fixedTimeRadioButton?.isSelected = settings.fixedTime

        setPanelEnabled(timePanel, settings.fixedTime && !settings.version4)

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
                    if (isUuid7TimeComponent(uiComponent)
                    ) {
                        setPanelEnabled(timePanel, isFixedTime() && isUuidVersion7())
                        updateIds()
                    }
                    updatePreview()
                }
            }
        // Time spinners.
        sequenceOf(
            yearSpinner,
            monthSpinner,
            daySpinner,
            hourSpinner,
            minuteSpinner,
            secondSpinner,
            millisSpinner
        )
            .filterNotNull()
            .forEach { uiComponent: JSpinner? ->
                uiComponent?.addChangeListener {
                    if (isVersion7() == true && isFixedTime()) {
                        updateIds()
                        updatePreview()
                    }
                }
            }

        numberInputField?.addChangeListener {
            updateIds()
            updatePreview()
        }

        sequenceOf(
            separatorInputField,
            prefixInputField,
            suffixInputField
        )
            .filterNotNull()
            .forEach {
                it.addKeyListener(object : KeyListener {
                    override fun keyTyped(e: KeyEvent?) = Unit
                    override fun keyPressed(e: KeyEvent?) = Unit
                    override fun keyReleased(e: KeyEvent?) {
                        updatePreview()
                    }
                })
            }

        updateIds()
        updatePreview()
    }

    private fun updateIds() {
        this.currentIds = computeIds()
    }

    private fun updatePreview() {
        val previewSettings =
            UUIDGeneratorPopupSettings()
        applyToSettings(previewSettings)

        val result = currentIds.joinToString(
            separator = unescapedOrEmpty(separatorInputField?.text),
            prefix = unescapedOrEmpty(prefixInputField?.text),
            postfix = unescapedOrEmpty(suffixInputField?.text)
        ) {
            UUIDGenerator.formatUUID(it, previewSettings)
        }

        resultOutputField?.text = result
    }

    fun getOutput() = resultOutputField?.text

    private fun unescapedOrEmpty(textInput: String?): String {
        return textInput
            ?.replace("\\n", "\n")
            ?.replace("\\r", "\r")
            ?.replace("\\t", "\t")
            ?: ""
    }

    private fun computeIds(): List<UUID> {
        val numberToGenerate = getNumberToGenerate() ?: 1
        return (1..numberToGenerate)
            .asSequence()
            .map { createRandomUuidBasedOnSettings() }
            .toList()
    }

    private fun createRandomUuidBasedOnSettings(): UUID {
        return if (isVersion4() ?: true) {
            UuidCreator.getRandomBased()
        } else {
            if (isFixedTime()) {
                timeFieldsToUuidv7()
            } else {
                UuidCreator.getTimeOrderedEpoch()
            }
        }
    }

    private fun getNumberToGenerate(): Int? {
        return numberInputField?.value
            ?.let {
                if (it is Int) {
                    it
                } else {
                    null
                }
            }
            ?.takeIf { it > 0 }
    }

    fun applyToSettings(settings: UUIDGeneratorPopupSettings) {
        settings.version4 = isVersion4() ?: true
        settings.lowerCased = isLowerCased() ?: true
        settings.withDashes = isWithDashes() ?: true
        settings.longSize = isLongSize() ?: true
        settings.numberFieldValue = getNumberToGenerate() ?: 1
        settings.separatorFieldValue = separatorInputField?.text ?: ""
        settings.prefixFieldValue = prefixInputField?.text ?: ""
        settings.suffixFieldValue = suffixInputField?.text ?: ""
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
}
