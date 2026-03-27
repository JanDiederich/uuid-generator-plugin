package com.github.leomillon.uuidgenerator.annotator.uuid

import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.exception.InvalidUuidException
import com.github.f4b6a3.uuid.util.UuidUtil
import com.github.leomillon.uuidgenerator.parser.UUID_WITH_DASHES_LENGTH
import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.github.leomillon.uuidgenerator.parser.textRange
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDReformatQuickFix
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDToggleDashesQuickFix
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDv4RandomQuickFix
import com.github.leomillon.uuidgenerator.quickfix.CopyStringToClipboardQuickFix
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDv7RandomQuickFix
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UUIDDefaultAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        if (!UUIDGeneratorSettings.instance.codeHighlighting) return

        val rawText = (element as? LeafPsiElement)
            ?.takeIf { it.textLength >= UUID_WITH_DASHES_LENGTH }
            ?.text
            ?.takeIf { it.isNotBlank() }
            ?: return

        highlightInText(element, rawText, holder)
    }
}

private const val uuidV7TimePattern = "yyyy-MM-dd HH:mm:ss.SSS XXX '['VV']'"
private val uuidV7Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(uuidV7TimePattern)!!

private fun highlightInText(
    element: PsiElement,
    rawText: String,
    holder: AnnotationHolder
) {
    val startOffset = element.textRange.startOffset
    rawText.findUUIDs().forEach { (matchingValue, range) ->
        val textRange = range.textRange(startOffset)

        var message = "UUID"
        var uuidVersion = 4
        var uuidV7TimeFormatted: String? = null
        var uuidV7TimeMillis: String? = null
        try {
            val uuid = UuidCreator.fromString(matchingValue)
            uuidVersion = uuid.version()
            message = if (uuidVersion == 7) {
                val uuidV7Instant = UuidUtil.getInstant(uuid)
                val zonedDateTime = uuidV7Instant.atZone(ZoneId.systemDefault())
                uuidV7TimeFormatted = zonedDateTime.format(uuidV7Formatter)
                uuidV7TimeMillis = uuidV7Instant.toEpochMilli().toString()
                "UUIDv7 Timestamp: $uuidV7TimeFormatted"
            } else {
                "UUIDv${uuidVersion}"
            }
        } catch (_: InvalidUuidException) {
            // Ignore this, this is a not unlikely and acceptable outcome.
        }

        val annotationBuilder: AnnotationBuilder = holder.newAnnotation(HighlightSeverity.INFORMATION, message)
            .range(textRange)
            .enforcedTextAttributes(DefaultLanguageHighlighterColors.CONSTANT.defaultAttributes)

        if (uuidVersion == 7) {
            /* While the menu entries sre always sorted alphabetically,
            put this first to signal IntelliJ that this should be available as tooltip quickfix. */
            annotationBuilder
                .withFix(UUIDv7RandomQuickFix(textRange))
                .withFix(UUIDv4RandomQuickFix(textRange))
            if (uuidV7TimeFormatted != null) {
                annotationBuilder.withFix(
                    CopyStringToClipboardQuickFix(
                        "Copy UUIDv7 formatted timestamp to clipboard",
                        uuidV7TimeFormatted
                    )
                )
            }
            if (uuidV7TimeMillis != null) {
                annotationBuilder.withFix(
                    CopyStringToClipboardQuickFix(
                        "Copy UUIDv7 timestamp epoch milliseconds to clipboard",
                        uuidV7TimeMillis
                    )
                )
            }
        } else {
            annotationBuilder
                .withFix(UUIDv4RandomQuickFix(textRange))
                .withFix(UUIDv7RandomQuickFix(textRange))
        }
        annotationBuilder
            .withFix(UUIDReformatQuickFix(matchingValue, textRange))
            .withFix(UUIDToggleDashesQuickFix(matchingValue, textRange))
            .create()
    }
}
