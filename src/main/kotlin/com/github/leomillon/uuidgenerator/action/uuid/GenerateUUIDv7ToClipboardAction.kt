package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.action.GenerateToClipboardAction

/**
 * Action that generate a UUIDv7 into clipboard.
 *
 * @author Jan-Hendrik Diederich
 */
class GenerateUUIDv7ToClipboardAction : GenerateToClipboardAction() {
    override fun generateId(): String = UUIDGenerator.generateUUIDv7()
}
