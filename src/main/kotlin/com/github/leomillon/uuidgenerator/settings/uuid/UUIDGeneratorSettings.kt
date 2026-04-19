package com.github.leomillon.uuidgenerator.settings.uuid

import com.github.leomillon.uuidgenerator.FixedTimeGenerating
import com.github.leomillon.uuidgenerator.settings.LocalDateTimeConverter
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime

@State(name = "UUIDGeneratorSettings", storages = [(Storage("uuid_generator.xml"))])
class UUIDGeneratorSettings : PersistentStateComponent<UUIDGeneratorSettings>,
    UUIDFormatSettings, FixedTimeGenerating {

    companion object {
        val instance: UUIDGeneratorSettings
            get() = service()
    }

    var version = "Unknown"

    /**
     * Default values
     */
    var uuidVersion4 = true
    var lowerCased = true
    var withDashes = true
    var longSize = true
    var codeHighlighting = true

    /** Is the current time used as base for UUIDv7 or a fixed time. */
    var fixedTime = true

    @OptionTag(converter = LocalDateTimeConverter::class)
    var uuidCreationTime: LocalDateTime? = null

    @Nullable
    override fun getState() = this

    override fun loadState(state: UUIDGeneratorSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun isLowerCased() = lowerCased

    override fun isWithDashes() = withDashes

    override fun isLongSize() = longSize

    override fun uuidCreationTime() = uuidCreationTime
}
