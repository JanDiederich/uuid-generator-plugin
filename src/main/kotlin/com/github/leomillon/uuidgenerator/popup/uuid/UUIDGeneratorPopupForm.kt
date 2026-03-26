package com.github.leomillon.uuidgenerator.popup.uuid

import com.github.f4b6a3.uuid.UuidCreator
import com.github.leomillon.uuidgenerator.UUIDGenerator
import kotlinx.datetime.number
import java.awt.Component
import java.awt.ItemSelectable
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.swing.*


class UUIDGeneratorPopupForm {

    private var panel: JPanel? = null
    private var version4RadioButton: JRadioButton? = null
    private var version7RadioButton: JRadioButton? = null
    private var lowerCaseRadioButton: JRadioButton? = null
    private var upperCaseRadioButton: JRadioButton? = null
    private var withDashesRadioButton: JRadioButton? = null
    private var withoutDashesRadioButton: JRadioButton? = null
    private var longSizeRadioButton: JRadioButton? = null
    private var shortSizeRadioButton: JRadioButton? = null
    private var numberInputField: JSpinner? = null
    private var separatorInputField: JTextField? = null
    private var prefixInputField: JTextField? = null
    private var suffixInputField: JTextField? = null
    private var resultOutputField: JTextArea? = null

    private var currentTimeRadioButton: JRadioButton? = null
    private var fixedTimeRadioButton: JRadioButton? = null
    private var timePanel: JPanel? = null
    private var yearSpinner: JSpinner? = null
    private var monthSpinner: JSpinner? = null
    private var daySpinner: JSpinner? = null
    private var hourSpinner: JSpinner? = null
    private var minuteSpinner: JSpinner? = null
    private var secondSpinner: JSpinner? = null
    private var millisSpinner: JSpinner? = null

    private var currentIds = listOf<UUID>()

    private val settings: UUIDGeneratorPopupSettings = UUIDGeneratorPopupSettings.instance

    init {
        loadSettings()
    }

    private fun loadSettings() {
        version4RadioButton?.isSelected = settings.version4
        version7RadioButton?.isSelected = !settings.version4
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

        currentTimeRadioButton?.isSelected = settings.currentTime
        fixedTimeRadioButton?.isSelected = !settings.currentTime

        setPanelEnabled(timePanel, !settings.currentTime)

        yearSpinner?.value = settings.year
        monthSpinner?.value = settings.month
        daySpinner?.value = settings.day
        hourSpinner?.value = settings.hour
        minuteSpinner?.value = settings.minute
        secondSpinner?.value = settings.second
        millisSpinner?.value = settings.millis

        sequenceOf<ItemSelectable?>(
            version4RadioButton,
            version7RadioButton,
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
                    if (uiComponent == currentTimeRadioButton
                        || uiComponent == fixedTimeRadioButton
                    ) {
                        setPanelEnabled(timePanel, isFixedTime())
                    }
                    if (uiComponent == version4RadioButton
                        || uiComponent == version7RadioButton
                        || uiComponent == currentTimeRadioButton
                        || uiComponent == fixedTimeRadioButton
                    ) {
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
                UuidCreator.getTimeOrderedEpoch(
                    OffsetDateTime.of(
                        year(), month(), dayOfMonth(),
                        hour(), minute(), second(), millis() * 1_000_000,
                        ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())
                    ).toInstant()
                )
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

        settings.year = year()
        settings.month = month()
        settings.day = dayOfMonth()
        settings.hour = hour()
        settings.minute = minute()
        settings.second = second()
        settings.millis = millis()
    }

    private fun isFixedTime(): Boolean = fixedTimeRadioButton?.isSelected ?: false
    private fun year(): Int = (yearSpinner?.value ?: OffsetDateTime.now().year) as Int
    private fun month(): Int = (monthSpinner?.value ?: OffsetDateTime.now().month) as Int
    private fun dayOfMonth(): Int = (daySpinner?.value ?: OffsetDateTime.now().dayOfMonth) as Int
    private fun hour(): Int = (hourSpinner?.value ?: OffsetDateTime.now().hour) as Int
    private fun minute(): Int = (minuteSpinner?.value ?: OffsetDateTime.now().minute) as Int
    private fun second(): Int = (secondSpinner?.value ?: OffsetDateTime.now().second) as Int
    private fun millis(): Int = (millisSpinner?.value ?: (OffsetDateTime.now().nano / 1_000_000)) as Int

    fun component(): JComponent? = panel

    private fun isVersion4() = version4RadioButton?.isSelected
    private fun isVersion7() = version7RadioButton?.isSelected
    private fun isLowerCased() = lowerCaseRadioButton?.isSelected
    private fun isWithDashes() = withDashesRadioButton?.isSelected
    private fun isLongSize() = longSizeRadioButton?.isSelected

    fun setPanelEnabled(panel: JPanel?, isEnabled: Boolean) {
        if (panel == null) return;
        panel.setEnabled(isEnabled)
        val components: Array<Component> = panel.components
        for (component in components) {
            if (component is JPanel) {
                setPanelEnabled((component as JPanel?), isEnabled)
            }
            component.isEnabled = isEnabled
        }
    }

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
