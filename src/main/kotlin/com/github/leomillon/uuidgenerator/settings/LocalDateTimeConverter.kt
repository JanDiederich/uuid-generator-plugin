package com.github.leomillon.uuidgenerator.settings

import com.intellij.util.xmlb.Converter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeConverter : Converter<LocalDateTime>() {
    override fun fromString(value: String): LocalDateTime? {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    override fun toString(value: LocalDateTime): String? {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}