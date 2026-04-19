package com.github.leomillon.uuidgenerator

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

/** Implemented by the generator popup, and the settings for fixed time UUIDv7 generating. */
interface FixedTimeGenerating {
    fun uuidCreationTime(): LocalDateTime?

    fun timeFieldsToUuidv7(): UUID {
        val creationTime = uuidCreationTime()
        val time: Instant = if (creationTime == null) {
            toInstant(LocalDateTime.now())
        } else {
            toInstant(creationTime)
        }
        return UuidCreator.getTimeOrderedEpoch(time)
    }

    fun toInstant(time: LocalDateTime): Instant =
        time.toInstant(ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now()))
}