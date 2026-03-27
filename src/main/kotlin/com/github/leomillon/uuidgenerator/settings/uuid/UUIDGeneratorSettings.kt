package com.github.leomillon.uuidgenerator.settings.uuid

import com.github.leomillon.uuidgenerator.FixedTimeGenerating
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.datetime.number
import org.jetbrains.annotations.Nullable
import java.time.OffsetDateTime

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
    var year: Int = OffsetDateTime.now().year
    var month: Int = OffsetDateTime.now().month.number
    var day: Int = OffsetDateTime.now().dayOfMonth
    var hour: Int = OffsetDateTime.now().hour
    var minute: Int = OffsetDateTime.now().minute
    var second: Int = OffsetDateTime.now().second
    var millis: Int = OffsetDateTime.now().nano / 1_000_000

    @Nullable
    override fun getState() = this

    override fun loadState(state: UUIDGeneratorSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun isLowerCased() = lowerCased

    override fun isWithDashes() = withDashes

    override fun isLongSize() = longSize

    override fun year() = year
    override fun month() = month
    override fun dayOfMonth() = day
    override fun hour() = hour
    override fun minute() = minute
    override fun second() = second
    override fun millis() = millis
}
