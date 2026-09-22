package com.github.leomillon.uuidgenerator

import com.github.f4b6a3.uuid.UuidCreator
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/** Implemented by the generator popup, and the settings for fixed time UUIDv7 generating. */
interface FixedTimeGenerating {
    fun fetchUuidCreationTime(): LocalDateTime?

    fun timeFieldsToUuidv7(): UUID {
        val creationTime = fetchUuidCreationTime()
        val time: Instant = if (creationTime == null) {
            toInstant(LocalDateTime.now())
        } else {
            toInstant(creationTime)
        }
        return UuidCreator.getTimeOrderedEpoch(time)
    }

    fun toInstant(time: LocalDateTime): Instant =
        time.atZone(java.time.ZoneId.systemDefault()).toInstant()
}