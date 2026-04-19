package com.github.leomillon.uuidgenerator.popup.uuid.generator

import com.github.leomillon.uuidgenerator.settings.LocalDateTimeConverter
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDFormatSettings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime

@State(name = "UUIDGeneratorPopupSettings", storages = [(Storage("uuid_popup.xml"))])
class UUIDGeneratorPopupSettings : PersistentStateComponent<UUIDGeneratorPopupSettings>,
    UUIDFormatSettings {

    companion object {
        val instance: UUIDGeneratorPopupSettings
            get() = service()
    }

    /**
     * Default values
     */
    var uuidVersion4 = true
    var lowerCased = true
    var withDashes = true
    var longSize = true
    var numberFieldValue = 1
    var separatorFieldValue = "\\n"
    var prefixFieldValue = ""
    var suffixFieldValue = ""

    /** Is the current time used as base for UUIDv7 or a fixed time. */
    var fixedTime = true

    @OptionTag(converter = LocalDateTimeConverter::class)
    var uuidCreationTime: LocalDateTime? = null

    @Nullable
    override fun getState() = this

    override fun loadState(state: UUIDGeneratorPopupSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun isLowerCased() = lowerCased

    override fun isWithDashes() = withDashes

    override fun isLongSize() = longSize
}
