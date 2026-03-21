package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.UUIDGenerator

/**
 * Replace found UUIDs in selection by new ones action.<br>
 * Implements UUID version specific string generation.
 *
 * @author Jan-Hendrik Diederich
 */
class ReplaceUuidV4sInSelectionAction : ReplaceUUIDsInSelectionAction() {

    override fun generateReplacementUUID(): String {
        return UUIDGenerator.generateUUIDv4()
    }
}
