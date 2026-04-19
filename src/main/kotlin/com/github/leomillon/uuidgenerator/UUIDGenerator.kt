package com.github.leomillon.uuidgenerator

import com.github.f4b6a3.uuid.UuidCreator
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDFormatSettings
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import java.time.LocalDateTime
import java.util.*

object UUIDGenerator {

    fun generateUUIDv4() =
        generateUUIDv4(UUIDGeneratorSettings.instance)

    fun generateUUIDv4(settings: UUIDGeneratorSettings): String {
        return formatUUID(
            /* Use this to explicitly get a UUIDv4. Even when the JDK would switch to UUIDv7,
             this will still generate a UUIDv4. This method is explicitly named "generateUUID__v4__()",
             not "generateUUID()". */
            UuidCreator.getRandomBased(),
            settings
        )
    }

    fun generateUUIDv7() =
        generateUUIDv7(UUIDGeneratorSettings.instance)

    fun generateUUIDv7(settings: UUIDGeneratorSettings): String {
        val time: UUID
        if (settings.fixedTime) {
            time = settings.timeFieldsToUuidv7()
        } else {
            time = UuidCreator.getTimeOrderedEpoch()
        }
        return formatUUID(
            time,
            settings
        )
    }

    fun formatUUID(id: UUID, generatorSettings: UUIDFormatSettings): String {
        var formattedId = id.toString()

        if (!generatorSettings.isLongSize()) {
            formattedId = formattedId.substringBefore('-')
        }

        if (!generatorSettings.isLowerCased()) {
            formattedId = formattedId.uppercase(Locale.getDefault())
        }

        if (!generatorSettings.isWithDashes()) {
            formattedId = formattedId.replace("-", "")
        }

        return formattedId
    }
}
