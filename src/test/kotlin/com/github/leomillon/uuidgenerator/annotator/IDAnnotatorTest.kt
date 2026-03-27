package com.github.leomillon.uuidgenerator.annotator

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.github.f4b6a3.ulid.Ulid
import com.github.leomillon.uuidgenerator.parser.IdType
import com.github.leomillon.uuidgenerator.settings.cuid.CUIDGeneratorSettings
import com.github.leomillon.uuidgenerator.settings.ulid.ULIDGeneratorSettings
import com.github.leomillon.uuidgenerator.settings.uuid.UUIDGeneratorSettings
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.json.JsonFileType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.idea.KotlinFileType
import java.time.ZoneId
import java.util.*

private const val targetUUIDv4 = "037d596f-0740-48d5-a5ec-8b4948f9e561"
private const val targetUUIDv7 = "019d2220-935d-7342-af16-026657bc1f29"
private const val targetULID = "01EGNR5DS9WY7VG9YPT0TXDCA5"
private const val targetCUID = "ckl87igpp000701jp3u7c04r7"
private val ulidDateTime = Ulid.getInstant(targetULID).atZone(ZoneId.systemDefault())

class IDAnnotatorTest : BasePlatformTestCase() {

    override fun tearDown() {
        super.tearDown()
        UUIDGeneratorSettings.instance.also {
            it.codeHighlighting = true
        }
        ULIDGeneratorSettings.instance.also {
            it.codeHighlighting = true
        }
        CUIDGeneratorSettings.instance.also {
            it.codeHighlighting = true
        }
    }

    fun `test should annotate UUIDs, ULIDs and CUIDs in Java code`() {

        // Given
        @Language("Java")
        val code = """
        class SomeJava {
            public static void main(String[] args) {
                System.out.println("Here is some UUIDv4 : $targetUUIDv4");
                System.out.println("Here is some UUIDv7 : $targetUUIDv7");
                System.out.println("Here is some ULID : $targetULID");
                System.out.println("Here is some CUID : $targetCUID");
                
                new FoooooooooooooooooooBaaaaaaaaaaa();
            }
            
            public static class FoooooooooooooooooooBaaaaaaaaaaa {
                public void someLongMethodNameeeeeeeeeeeeeee() {}
                public void someotherlongmethodnameeeeeeeeee() {}
            }
            
            /**
             * False positive highlight of CUID #40
             * <a href="https://github.com/leomillon/uuid-generator-plugin/issues/40">Issue #40</a>
             */
            public static class TicketTriggerHiredFlowAtCalculator {
            }
            
            /**
             * False positive highlight of CUID #40
             * <a href="https://github.com/leomillon/uuid-generator-plugin/issues/40">Issue #40</a>
             */
            public static class ClassToTestCUIDHighlight1 {
            }
        }
        """.trimIndent()
        myFixture.configureByText(JavaFileType.INSTANCE, code)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        /* Set a timezone for the UUIDv7 localized time string
        - to make the test run everywhere, regardless of the test-systems set timezone. */
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))

        // When
        val highlightingResult = myFixture.doHighlighting()

        // Then
        assertThat(highlightingResult).isNotEmpty()
        assertUUIDv4Highlight(highlightingResult, 112, 148)
        assertUUIDv7Highlight(highlightingResult, 202, 238)
        assertULIDHighlight(highlightingResult, 290, 316)
        assertCUIDHighlight(highlightingResult, 368, 393)
        assertUUIDCount(highlightingResult, 4, 1)
        assertUUIDCount(highlightingResult, 7, 1)
        assertULIDCount(highlightingResult, 1)
        assertCUIDCount(highlightingResult, 1)
    }

    fun `test should annotate UUIDs, ULIDs and CUIDs in Kotlin code`() {

        // Given
        @Language("kotlin")
        val code = """
        fun main(args: Array<String>) {
            println("Here is some UUID : $targetUUIDv4")
            println("Here is some ULID : $targetULID")
            println("Here is some CUID : $targetCUID")
        }
        """.trimIndent()
        myFixture.configureByText(KotlinFileType.INSTANCE, code)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // When
        val highlightingResult = myFixture.doHighlighting()

        // Then
        assertThat(highlightingResult, "highlightingResult").isNotEmpty()
        assertUUIDv4Highlight(highlightingResult, 65, 101)
        assertULIDHighlight(highlightingResult, 137, 163)
        assertCUIDHighlight(highlightingResult, 199, 224)
        assertUUIDCount(highlightingResult, 4, 1)
        assertUUIDCount(highlightingResult, 7, 0)
        assertULIDCount(highlightingResult, 1)
        assertCUIDCount(highlightingResult, 1)
    }

    fun `test should annotate UUIDs, ULIDs and CUIDs in JSON code`() {

        @Language("JSON")
        val code = """
        {
          "someField": "$targetUUIDv4",
          "someOtherField": [
            "$targetULID"
          ],
          "someOtherField2": "$targetCUID"
        }
        """.trimIndent()
        // Given
        myFixture.configureByText(JsonFileType.INSTANCE, code)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // When
        val highlightingResult = myFixture.doHighlighting()

        // Then
        assertUUIDv4Highlight(highlightingResult, 18, 54)
        assertULIDHighlight(highlightingResult, 84, 110)
        assertCUIDHighlight(highlightingResult, 139, 164)
        assertUUIDCount(highlightingResult, 4, 1)
        assertUUIDCount(highlightingResult, 7, 0)
        assertULIDCount(highlightingResult, 1)
        assertCUIDCount(highlightingResult, 1)
    }

    fun `test should not annotate UUIDs, ULIDs and CUIDs in code if highlighting disabled in settings`() {

        // Given
        UUIDGeneratorSettings.instance.also {
            it.codeHighlighting = false
        }
        ULIDGeneratorSettings.instance.also {
            it.codeHighlighting = false
        }
        CUIDGeneratorSettings.instance.also {
            it.codeHighlighting = false
        }
        @Language("JSON")
        val code = """
        {
          "someField": "$targetUUIDv4",
          "someOtherField": [
            "$targetULID"
          ],
          "someOtherField2": "$targetCUID"
        }
        """.trimIndent()
        myFixture.configureByText(JsonFileType.INSTANCE, code)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // When
        val highlightingResult = myFixture.doHighlighting()

        // Then
        assertUUIDCount(highlightingResult, 4, 0)
        assertUUIDCount(highlightingResult, 7, 0)
        assertULIDCount(highlightingResult, 0)
        assertCUIDCount(highlightingResult, 0)
    }

    private fun assertUUIDCount(highlightingResult: List<HighlightInfo>, version: Int, count: Int) {
        assertThat(highlightingResult.filter {
            it.description != null && it.description.startsWith("UUIDv${version}")
        }.size)
            .isEqualTo(count)
    }

    private fun assertULIDCount(highlightingResult: List<HighlightInfo>, count: Int) {
        assertThat(highlightingResult.filter { it.description?.startsWith("ULID") ?: false }.size).isEqualTo(count)
    }

    private fun assertCUIDCount(highlightingResult: List<HighlightInfo>, count: Int) {
        assertThat(highlightingResult.filter { it.description?.startsWith("CUID") ?: false }.size).isEqualTo(count)
    }

    private fun assertUUIDv4Highlight(highlightingResult: List<HighlightInfo>, rangeStart: Int, rangeEnd: Int) {
        assertUUIDHighlight(IdType.UUIDv4, highlightingResult, rangeStart, rangeEnd, targetUUIDv4, "UUIDv4")
    }

    private fun assertUUIDv7Highlight(highlightingResult: List<HighlightInfo>, rangeStart: Int, rangeEnd: Int) {
        assertUUIDHighlight(
            IdType.UUIDv7,
            highlightingResult,
            rangeStart,
            rangeEnd,
            targetUUIDv7,
            "UUIDv7 Timestamp: 2026-03-25 00:14:13.469 +01:00 [Europe/Berlin]"
        )
    }

    private fun assertUUIDHighlight(
        idType: IdType,
        highlightingResult: List<HighlightInfo>,
        rangeStart: Int,
        rangeEnd: Int,
        targetText: String,
        expectedDescription: String
    ) {
        val highlight = highlightingResult.find { it.text == targetText }
        assertThat(highlight).isNotNull()
        with(highlight!!) {
            assertThat(description).isEqualTo(expectedDescription)
            assertThat(severity).isEqualTo(HighlightSeverity.INFORMATION)
            assertThat(highlighter.textRange.startOffset).isEqualTo(rangeStart)
            assertThat(highlighter.textRange.endOffset).isEqualTo(rangeEnd)

            val quickFixes: List<String> = buildList {
                findRegisteredQuickFix { desc, _ -> add(desc.action.text); null }
            }
            assertThat(quickFixes, "quickFixes").isNotEmpty()
            if (idType == IdType.UUIDv4) {
                assertThat(quickFixes, "quickFixes")
                    .containsOnly(
                        "Replace with new random UUIDv4",
                        "Replace with new random UUIDv7",
                        "Reformat with your UUID settings",
                        "Toggle dashes"
                    )
            } else {
                assertThat(quickFixes, "quickFixes")
                    .containsOnly(
                        "Replace with new random UUIDv7",
                        "Replace with new random UUIDv4",
                        "Copy UUIDv7 formatted timestamp to clipboard",
                        "Copy UUIDv7 timestamp epoch milliseconds to clipboard",
                        "Reformat with your UUID settings",
                        "Toggle dashes"
                    )
            }
        }
    }

    private fun assertULIDHighlight(highlightingResult: List<HighlightInfo>, rangeStart: Int, rangeEnd: Int) {
        val highlight = highlightingResult.find { it.text == targetULID }
        assertThat(highlight).isNotNull()
        with(highlight!!) {
            assertThat(description).isEqualTo("ULID Timestamp: 1598457820 ($ulidDateTime)")
            assertThat(severity).isEqualTo(HighlightSeverity.INFORMATION)
            assertThat(highlighter.textRange.startOffset).isEqualTo(rangeStart)
            assertThat(highlighter.textRange.endOffset).isEqualTo(rangeEnd)

            val quickFixes: List<String> = buildList {
                findRegisteredQuickFix { desc, _ -> add(desc.action.text); null }
            }
            assertThat(quickFixes, "quickFixes").isNotEmpty()
            assertThat(quickFixes, "quickFixes")
                .containsOnly(
                    "Replace with new random ULID"
                )
        }
    }

    private fun assertCUIDHighlight(highlightingResult: List<HighlightInfo>, rangeStart: Int, rangeEnd: Int) {
        val highlight = highlightingResult.find { it.text == targetCUID }
        assertThat(highlight).isNotNull()
        with(highlight!!) {
            assertThat(severity).isEqualTo(HighlightSeverity.INFORMATION)
            assertThat(highlighter.textRange.startOffset).isEqualTo(rangeStart)
            assertThat(highlighter.textRange.endOffset).isEqualTo(rangeEnd)

            val quickFixes: List<String> = buildList {
                findRegisteredQuickFix { desc, _ -> add(desc.action.text); null }
            }
            assertThat(quickFixes, "quickFixes").isNotEmpty()
            assertThat(quickFixes, "quickFixes")
                .containsOnly(
                    "Replace with new random CUID",
                    "Reformat with your CUID settings"
                )
        }
    }
}
