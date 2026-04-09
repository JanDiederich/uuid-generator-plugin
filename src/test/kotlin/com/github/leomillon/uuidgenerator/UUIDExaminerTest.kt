package com.github.leomillon.uuidgenerator

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.*
import com.github.leomillon.uuidgenerator.popup.uuid.summarizeString
import com.jetbrains.rd.util.UUID
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

class UUIDExaminerTest {

    class TestParameter(
        var input: String,
        val markdownResult: String,
        val htmlResult: String,
        val headerInfo: HeaderInfo,
        var examinationResults: List<ExaminationResult>,
    )

    companion object {
        const val uuidV4 = "fa23fba7-00ed-4eb2-94e7-685c2b2b39dc"
        const val uuidV4_2 = "04f98459-c585-430e-a746-0dd641150302"

        const val uuidV7 = "019d52d0-8b4d-72f5-a066-d329aae3652b"
        const val uuidV7_2 = "019d52d6-3dd9-7802-8174-aacd7a8b59aa"

        /* Add intentionally malformed, erroneous quoted strings,
           to make sure the parse handles it gracefully (looking especially at the Apache CSV Parser). */
        const val header =
            "ID, Street, Postal Code, District, \"Country, \"Age\", Gender, Whatever, Whoever, REFERENCE_ID"
        val line0 = """
                $uuidV4, "Northern Street", 12345, "Southern District", "State Country, "23", Male, null, null, $uuidV4_2
            """.trim()
        val line1 = """
                $uuidV7, "Loch Golf", 67890, "Sand, Place",    "Land-Land, "12", Female, null, null, $uuidV7_2
            """.trim()
        const val uuidV7Time = "2026-04-03T10:08:12.109Z"
        val uuidV7Instant: Instant = Instant.parse(uuidV7Time)

        const val uuidV7Time2 = "2026-04-03T10:14:25.497Z"
        val uuidV7Instant2: Instant = Instant.parse(uuidV7Time2)

        @Suppress("unused")
        @JvmStatic
        fun expectedLineParsing(): List<TestParameter> {
            return listOf(
                TestParameter(
                    """
                $header
                $line0
                $line1
                """.trim(),
                    markdownResult = """| Source | UUID | Version | Timestamp | UUID | Version | Timestamp |
| --- | --- | --- | --- | --- | --- | --- |
| fa23fba7...41150302 | $uuidV4 | 4 | null | $uuidV4_2 | 4 | null |
| 019d52d0...7a8b59aa | $uuidV7 | 7 | $uuidV7Time | $uuidV7_2 | 7 | $uuidV7Time2 |
""",
                    // The size is so big, put this into an extra file.
                    htmlResult = UUIDExaminerTest::class.java.getResource("/examinerResult0.html")!!.readText(),
                    HeaderInfo(
                        singleTitles = listOf(
                            // 1st column: Source.
                            "Source",
                            // UUIO + UUID-Version + UUID-Timestamp.
                            "UUID", "Version", "Timestamp",
                            // UUIO + UUID-Version + UUID-Timestamp.
                            "UUID", "Version", "Timestamp"
                        ), headerGroupTitles = listOf(
                            HeaderGroupTitle("Source", 0, 1),
                            HeaderGroupTitle("ID", 1, 4),
                            // Skip all Non-UUID columns.
                            HeaderGroupTitle("REFERENCE_ID", 4, 7),
                        )
                    ),
                    listOf(
                        ExaminationResult(
                            header, listOf(
                                CellInfo(
                                    ColumnType.String, summarizeString(header)
                                ),
                                // uuidV4
                                CellInfo(ColumnType.Uuid, null),
                                CellInfo(ColumnType.Version, null),
                                // Timestamp comes from the UUIDv7 line.
                                CellInfo(ColumnType.Timestamp, null),
                                // uuidV4_2
                                CellInfo(ColumnType.Uuid, null),
                                CellInfo(ColumnType.Version, null),
                                CellInfo(ColumnType.Timestamp, null),
                            ), listOf(
                                UuidInfos("ID", null),
                                UuidInfos("Street", null),
                                UuidInfos("Postal Code", null),
                                UuidInfos("District", null),
                                UuidInfos("\"Country, \"Age\"", null),
                                UuidInfos("Gender", null),
                                UuidInfos("Whatever", null),
                                UuidInfos("Whoever", null),
                                UuidInfos("REFERENCE_ID", null),
                            )
                        ), ExaminationResult(
                            line0, listOf(
                                CellInfo(
                                    ColumnType.String, summarizeString(line0)
                                ),
                                // uuidV4
                                CellInfo(ColumnType.Uuid, UUID.fromString(uuidV4)),
                                CellInfo(ColumnType.Version, 4),
                                // Timestamp comes from the UUIDv7 line.
                                CellInfo(ColumnType.Timestamp, null),
                                // uuidV4_2
                                CellInfo(ColumnType.Uuid, UUID.fromString(uuidV4_2)),
                                CellInfo(ColumnType.Version, 4),
                                CellInfo(ColumnType.Timestamp, null),
                            ), listOf(
                                UuidInfos(
                                    uuidV4, listOf(
                                        UuidInfo(uuidV4, UUID.fromString(uuidV4), null)
                                    )
                                ),
                                // Northern Street
                                UuidInfos("Northern Street", null),
                                // 12345
                                UuidInfos("12345", null),
                                // State Country // 23
                                UuidInfos("Southern District", null), UuidInfos("\"State Country, \"23\"", null),
                                // Male
                                UuidInfos("Male", null),
                                // null
                                UuidInfos("null", null),
                                // null
                                UuidInfos("null", null),
                                // uuidV4_2
                                UuidInfos(
                                    uuidV4_2, listOf(UuidInfo(uuidV4_2, UUID.fromString(uuidV4_2), null))
                                )
                            )
                        ), ExaminationResult(
                            line1, listOf(
                                CellInfo(
                                    ColumnType.String, summarizeString(line1)
                                ),
                                // uuidV7
                                CellInfo(ColumnType.Uuid, UUID.fromString(uuidV7)),
                                CellInfo(ColumnType.Version, 7),
                                // Timestamp comes from the UUIDv7 line.
                                CellInfo(ColumnType.Timestamp, uuidV7Instant),
                                // uuidV7_2
                                CellInfo(ColumnType.Uuid, UUID.fromString(uuidV7_2)),
                                CellInfo(ColumnType.Version, 7),
                                CellInfo(ColumnType.Timestamp, uuidV7Instant2),
                            ), listOf(
                                UuidInfos(
                                    uuidV7, listOf(
                                        UuidInfo(uuidV7, UUID.fromString(uuidV7), uuidV7Instant)
                                    )
                                ),
                                // Loch Golf
                                UuidInfos("Loch Golf", null),
                                // 67890
                                UuidInfos("67890", null),
                                // Sand, Place
                                UuidInfos("Sand, Place", null),
                                // Land-Land // 12
                                UuidInfos("\"Land-Land, \"12\"", null),
                                // Female
                                UuidInfos("Female", null),
                                // null
                                UuidInfos("null", null),
                                // null
                                UuidInfos("null", null),
                                // uuidV7_2
                                UuidInfos(
                                    uuidV7_2, listOf(UuidInfo(uuidV7_2, UUID.fromString(uuidV7_2), uuidV7Instant2))
                                )
                            )
                        )
                    ),
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("expectedLineParsing")
    fun `uuid examiner test`(
        testParameter: TestParameter,
    ) {
        val buildResult: BuildResult = convertInputToTableLines(
            testParameter.input,
            ",",
            trimWhitespace = true,
            hasHeader = true,
            hasHeaderSeparator = false,
            summarizeSource = true
        )
        val examinationResults = buildResult.examinationResults
        assertThat(examinationResults.size).isEqualTo(testParameter.examinationResults.size)
        // Assert line-wise for better error logs.
        examinationResults.forEachIndexed { index, result ->
            val examinationResult: ExaminationResult = testParameter.examinationResults[index]

            assertAll {
                assertThat(result.line).isEqualTo(examinationResult.line)
                assertThat(result.cells).isEqualTo(examinationResult.cells)
                assertThat(result.uuidList).isEqualTo(examinationResult.uuidList)
            }
        }

        val headerInfo = buildResult.headerInfo
        assertAll {
            assertThat(headerInfo.singleTitles).isEqualTo(testParameter.headerInfo.singleTitles)
            assertThat(headerInfo.headerGroupTitles).isEqualTo(testParameter.headerInfo.headerGroupTitles)
        }

        val markdown = buildResult.convertToMarkdown()
        assertAll {
            assertThat(markdown).isEqualTo(
                testParameter.markdownResult
            )
        }

        val html = buildResult.convertToHtml()
        assertAll {
            assertThat(html).isEqualTo(testParameter.htmlResult)
        }
    }

    // For manually testing, if the IntelliJ debugger live evaluation works.
    @Test
    fun testUuidDebugging() {
        val uuidV4: String = uuidV4
        val uuidV4Value: UUID = UUID.fromString(Companion.uuidV4)

        val uuidV7: String = uuidV7
        val uuidV7Value: UUID = UUID.fromString(Companion.uuidV7)
        println("UUID: $uuidV4 $uuidV4Value, $uuidV7 $uuidV7Value")
    }
}
