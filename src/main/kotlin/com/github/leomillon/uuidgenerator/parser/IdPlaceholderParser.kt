package com.github.leomillon.uuidgenerator.parser

import com.github.leomillon.uuidgenerator.CUIDGenerator
import com.github.leomillon.uuidgenerator.ULIDGenerator
import com.github.leomillon.uuidgenerator.UUIDGenerator

private const val ID_TYPE_GROUP_NAME = "idType"
private const val LABEL_GROUP_NAME = "label"
private val PLACEHOLDER_GLOBAL_REGEX =
    "#gen\\.(?<$ID_TYPE_GROUP_NAME>uuid|uuidv4|uuidv7|ulid|cuid)(\\.(?<$LABEL_GROUP_NAME>[a-zA-Z0-9_-]+?))?#".toRegex()

fun CharSequence.findIdPlaceholders() = PLACEHOLDER_GLOBAL_REGEX.findAll(this)
    .map { matchResult ->
        val idType = matchResult.groups[ID_TYPE_GROUP_NAME]
            ?.value
            ?.let {
                when (it) {
                    "uuid" -> IdType.UUIDv4
                    "uuidv4" -> IdType.UUIDv4
                    "uuidv7" -> IdType.UUIDv7
                    "ulid" -> IdType.ULID
                    "cuid" -> IdType.CUID
                    else -> null
                }
            }
            ?: return@map null

        IdPlaceholder(
            rawValue = matchResult.value,
            idType = idType,
            label = matchResult.groups[LABEL_GROUP_NAME]
                ?.value
                ?.takeIf { it.isNotBlank() }
        ) to matchResult.range
    }
    .filterNotNull()

data class IdPlaceholder(
    val rawValue: String,
    val idType: IdType,
    val label: String?
)

enum class IdType(val idGenerator: () -> String) {
    UUIDv4(UUIDGenerator::generateUUIDv4),
    UUIDv7(UUIDGenerator::generateUUIDv7),
    ULID(ULIDGenerator::generateULID),
    CUID(CUIDGenerator::generateCUID);
}
