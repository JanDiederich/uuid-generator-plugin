package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.action.GenerateToClipboardAction

/**
 * Action that generate a UUIDv4 into clipboard.
 *
 * @author Léo Millon
 */
class GenerateUUIDv4ToClipboardAction : GenerateToClipboardAction() {
    override fun generateId(): String = UUIDGenerator.generateUUIDv4()
}
