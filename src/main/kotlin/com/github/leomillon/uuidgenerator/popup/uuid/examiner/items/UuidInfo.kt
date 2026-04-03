package com.github.leomillon.uuidgenerator.popup.uuid.examiner.items

import java.time.Instant
import java.util.UUID

/** Actual UUID info per column. */
data class UuidInfo(
    val source: String, val uuid: UUID, val timestamp: Instant? = null
) {
    /**
     * Column count. 3 if the UUID has a timestamp. 2 otherwise.
     * The source shouldn't be displayed, it's already in the UUID.
     *  1. UUID
     *  2. Version
     *  3. Timestamp (if available)
     * </ul>
     */
    val columnCount: Int = if (timestamp != null) 3 else 2

    fun version(): Int {
        return uuid.version()
    }
}