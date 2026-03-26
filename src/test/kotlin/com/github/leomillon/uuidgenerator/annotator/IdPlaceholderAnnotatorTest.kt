package com.github.leomillon.uuidgenerator.annotator

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.github.leomillon.uuidgenerator.parser.textRange
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.end
import org.jetbrains.kotlin.idea.base.codeInsight.handlers.fixers.start

class IdPlaceholderAnnotatorTest : BasePlatformTestCase() {

    fun `test should annotate id placeholders in code`() {

        // Given
        @Language("Java")
        val code = """
        class SomeJava {
            public static void main(String[] args) {
            
                // ID placeholders
                System.out.println("Here is some UUID placeholder : #gen.uuid#");
                System.out.println("Here is some UUIDv4 placeholder : #gen.uuidv4#");
                System.out.println("Here is some UUIDv7 placeholder : #gen.uuidv7#");
                System.out.println("Here is some ULID placeholder : #gen.ulid#");
                System.out.println("Here is some CUID placeholder : #gen.cuid#");
                
                // Labeled placeholders
                System.out.println("#gen.uuid.label_1#");
                System.out.println("#gen.uuidv4.label_1#");
                System.out.println("#gen.uuidv7.label_1#");
                System.out.println("#gen.ulid.label_1#");
                System.out.println("#gen.cuid.label_1#");
                
                // Some invalid placeholders
                System.out.println("#gen.unknown#");
                System.out.println("#gen.uuid.invalid label#");
                System.out.println("#gen.uuidv4.invalid label#");
                System.out.println("#gen.uuidv7.invalid label#");
                System.out.println("#gen.uuid.invalid.label#");
            }
        }
        """.trimIndent()
        myFixture.configureByText(JavaFileType.INSTANCE, code)
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        // When
        val highlightingResult = myFixture.doHighlighting()

        // Then
        assertThat(highlightingResult).isNotEmpty()

        println("Found Highlights:")
        for (info in highlightingResult) {
            val description = info.description
            if (description == null
                || description.startsWith("Cannot resolve symbol")
            ) {
                continue;
            }
            val textRange: TextRange = info.highlighter.textRange
            // Convert TextRange to IntRange: "end - 1".
            println("${textRange.start}..${textRange.end - 1}\t$description")
        }

        val mustMatch = listOf(
            (154..163) to "Random UUIDv4 placeholder",
            (230..241) to "Random UUIDv4 placeholder",
            (308..319) to "Random UUIDv7 placeholder",
            (384..393) to "Random ULID placeholder",
            (458..467) to "Random CUID placeholder",
            (541..558) to "Random UUIDv4 placeholder labeled 'label_1'",
            (591..610) to "Random UUIDv4 placeholder labeled 'label_1'",
            (643..662) to "Random UUIDv7 placeholder labeled 'label_1'",
            (695..712) to "Random ULID placeholder labeled 'label_1'",
            (745..762) to "Random CUID placeholder labeled 'label_1'"
        )
        mustMatch
            .forEach { (range: IntRange, description: String) ->
                val highlight = highlightingResult.find {
                    it.highlighter.textRange == range.textRange()
                }
                assertThat(highlight, "\"$description\" at $range").isNotNull()
                assertThat(highlight?.description).isEqualTo(
                    description
                )
            }

        val placeholdersHighlights = highlightingResult
            .filter { it.description?.startsWith("Random ") ?: false }
            .toList()
        placeholdersHighlights
            .forEach { result: HighlightInfo ->
                val quickFixes: List<String> = buildList {
                    result.findRegisteredQuickFix { desc, _ -> add(desc.action.text); null }
                }
                assertThat(quickFixes, "quickFixes (result: ${result})")
                    .isNotEmpty()
                assertThat(quickFixes, "quickFixes (result: ${result})")
                    .containsOnly("Replace with new random value")
            }
        assertThat(placeholdersHighlights.count()).isEqualTo(mustMatch.size)
    }
}
