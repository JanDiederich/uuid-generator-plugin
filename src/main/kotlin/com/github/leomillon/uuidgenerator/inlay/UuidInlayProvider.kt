package com.github.leomillon.uuidgenerator.inlay

import com.github.f4b6a3.uuid.util.UuidUtil
import com.github.leomillon.uuidgenerator.annotator.uuid.formatInstant
import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.util.*

class UuidInlayProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector {
        return UuidInlayCollector()
    }
}

private class UuidInlayCollector : SharedBypassCollector {
    override fun collectFromElement(
        element: PsiElement,
        sink: InlayTreeSink
    ) {
        val rawText = (element as? LeafPsiElement)
            ?.text
            ?: return

        rawText.findUUIDs().forEach { (matchingValue, range) ->
            val uuid = try {
                UUID.fromString(matchingValue)
            } catch (_: IllegalArgumentException) {
                null
            }
            if (uuid != null) {
                val version = uuid.version()
                val timestamp = if (version == 1 || version == 7) {
                    try {
                        UuidUtil.getInstant(uuid)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } else {
                    null
                }

                val hint = buildString {
                    append("UUID v$version")
                    if (timestamp != null) append(" ${formatInstant(timestamp)}")
                }
                val position: InlayPosition = if (timestamp == null) {
                    InlineInlayPosition(range.first + element.textRange.startOffset, true)
                } else {
                    AboveLineIndentedPosition(range.first + element.textRange.startOffset)
                }
                sink.addPresentation(
                    position = position,
                    payloads = null,
                    tooltip = hint,
                    hintFormat = HintFormat.default,
                    builder = { text(hint) }
                )
            }
        }
    }
}
