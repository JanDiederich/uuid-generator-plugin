package com.github.leomillon.uuidgenerator

import com.github.f4b6a3.uuid.UuidCreator
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

/** Implemented by the generator popup, and the settings for fixed time UUIDv7 generating. */
interface FixedTimeGenerating {
    fun year(): Int
    fun month(): Int
    fun dayOfMonth(): Int
    fun hour(): Int
    fun minute(): Int
    fun second(): Int
    fun millis(): Int

    fun timeFieldsToUuidv7(): UUID {
        return UuidCreator.getTimeOrderedEpoch(
            OffsetDateTime.of(
                year(),
                month(),
                dayOfMonth(),
                hour(),
                minute(),
                second(),
                millis() * 1_000_000,
                ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())
            ).toInstant()
        )
    }
}