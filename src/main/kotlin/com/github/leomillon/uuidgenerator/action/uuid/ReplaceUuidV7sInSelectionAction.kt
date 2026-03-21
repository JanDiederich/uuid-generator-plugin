package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.DisplayMessageUtils
import com.github.leomillon.uuidgenerator.EditorDocumentUtils
import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.github.leomillon.uuidgenerator.parser.textRange
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.MessageType

/**
 * Replace found UUIDs in selection by new ones action.<br>
 * Implements UUID version specific string generation.
 *
 * @author Jan-Hendrik Diederich
 */
class ReplaceUuidV7sInSelectionAction : ReplaceUUIDsInSelectionAction() {

    override fun generateReplacementUUID(): String {
        return UUIDGenerator.generateUUIDv7()
    }
}
