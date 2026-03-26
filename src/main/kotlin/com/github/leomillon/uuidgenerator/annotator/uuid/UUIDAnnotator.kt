package com.github.leomillon.uuidgenerator.annotator.uuid

import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.exception.InvalidUuidException
import com.github.leomillon.uuidgenerator.parser.UUID_WITH_DASHES_LENGTH
import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.github.leomillon.uuidgenerator.parser.textRange
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDReformatQuickFix
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDToggleDashesQuickFix
import com.github.leomillon.uuidgenerator.quickfix.uuid.UUIDv4RandomQuickFix
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
import java.time.Instant
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
private val uuidV7Formatter = DateTimeFormatter.ofPattern(uuidV7TimePattern)

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
        try {
            val uuid = UuidCreator.fromString(matchingValue)
            uuidVersion = uuid.version()
            message = if (uuidVersion == 7) {
                val timestampMs = uuid.mostSignificantBits ushr 16
                val instant = Instant.ofEpochMilli(timestampMs)
                val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                "UUIDv7 Timestamp: ${zonedDateTime.format(uuidV7Formatter)}"
            } else {
                "UUIDv${uuidVersion}"
            }
        } catch (_: InvalidUuidException) {
            // Ignore this, this is a very likely outcome.
        }

        // TODO: Why is this "newRange" value needed? Why is it suddenly the relative range
        //       instead of the absolute range?
        //       Search for a breaking change in the IntelliJ Plugin update.
        val newRange = TextRange(0, textRange.length)
        val annotationBuilder: AnnotationBuilder = holder.newAnnotation(HighlightSeverity.INFORMATION, message)
            .range(textRange)
            .enforcedTextAttributes(DefaultLanguageHighlighterColors.CONSTANT.defaultAttributes)

        // The first entry is determined by the current version of the UUID.
        // TODO: Why is the order here ignored for the menu entries?
        if (uuidVersion == 7) {
            annotationBuilder
                .withFix(UUIDv7RandomQuickFix(newRange))
                .withFix(UUIDv4RandomQuickFix(newRange))
        } else {
            annotationBuilder
                .withFix(UUIDv4RandomQuickFix(newRange))
                .withFix(UUIDv7RandomQuickFix(newRange))
        }
        annotationBuilder
            .withFix(UUIDReformatQuickFix(matchingValue, newRange))
            .withFix(UUIDToggleDashesQuickFix(matchingValue, newRange))
            .create()
    }
}
