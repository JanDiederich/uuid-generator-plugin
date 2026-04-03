package com.github.leomillon.uuidgenerator.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class IdPlaceholderParserTest {

    @TestFactory
    fun `test placeholders in text`() = idPlaceholdersTextProvider()
        .map { (input: String, expectedMatches: List<Pair<IdPlaceholder, IntRange>>) ->
            dynamicTest("should find id placeholders in text") {
                assertThat(input.findIdPlaceholders().toMap()).isEqualTo(expectedMatches.toMap())
            }
        }

    private fun idPlaceholdersTextProvider() = listOf(
        "#gen.uuid#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuid#",
                idType = IdType.UUIDv4,
                label = null
            ) to 0..9
        ),
        "#gen.uuidv4#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuidv4#",
                idType = IdType.UUIDv4,
                label = null
            ) to 0..11
        ),
        "#gen.uuidv7#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuidv7#",
                idType = IdType.UUIDv7,
                label = null
            ) to 0..11
        ),
        "#gen.ulid#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.ulid#",
                idType = IdType.ULID,
                label = null
            ) to 0..9
        ),
        "#gen.cuid#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.cuid#",
                idType = IdType.CUID,
                label = null
            ) to 0..9
        ),

        "Hello you! #gen.uuid#, #gen.uuidv4#, #gen.uuidv7#, #gen.ulid#, #gen.cuid# Bye bye!" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuid#",
                idType = IdType.UUIDv4,
                label = null
            ) to 11..20,
            IdPlaceholder(
                rawValue = "#gen.uuidv4#",
                idType = IdType.UUIDv4,
                label = null
            ) to 23..34,
            IdPlaceholder(
                rawValue = "#gen.uuidv7#",
                idType = IdType.UUIDv7,
                label = null
            ) to 37..48,
            IdPlaceholder(
                rawValue = "#gen.ulid#",
                idType = IdType.ULID,
                label = null
            ) to 51..60,
            IdPlaceholder(
                rawValue = "#gen.cuid#",
                idType = IdType.CUID,
                label = null
            ) to 63..72
        ),
        "#gen.uuid.label_1-bis#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuid.label_1-bis#",
                idType = IdType.UUIDv4,
                label = "label_1-bis"
            ) to 0..21
        ),
        "#gen.uuidv4.label_1-bis#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuidv4.label_1-bis#",
                idType = IdType.UUIDv4,
                label = "label_1-bis"
            ) to 0..23
        ),
        "#gen.uuidv7.label_1-bis#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.uuidv7.label_1-bis#",
                idType = IdType.UUIDv7,
                label = "label_1-bis"
            ) to 0..23
        ),
        "#gen.ulid.label_1-bis#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.ulid.label_1-bis#",
                idType = IdType.ULID,
                label = "label_1-bis"
            ) to 0..21
        ),
        "#gen.cuid.label_1-bis#" to listOf(
            IdPlaceholder(
                rawValue = "#gen.cuid.label_1-bis#",
                idType = IdType.CUID,
                label = "label_1-bis"
            ) to 0..21
        ),
        "#gen.#" to emptyList(),
        "#gen.unknown#" to emptyList(),
        "#gen.uuid.invalid label#" to emptyList(),
        "#gen.uuid.invalid.label#" to emptyList(),
        "#gen.uuidv4.invalid.label#" to emptyList(),
        "#gen.uuidv7.invalid.label#" to emptyList(),
        "#gen.ulid.invalid label#" to emptyList(),
        "#gen.ulid.invalid.label#" to emptyList(),
        "#gen.cuid.invalid label#" to emptyList(),
        "#gen.cuid.invalid.label#" to emptyList()
    )
}
