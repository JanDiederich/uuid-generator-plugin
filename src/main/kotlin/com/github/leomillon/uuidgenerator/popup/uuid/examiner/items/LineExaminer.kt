package com.github.leomillon.uuidgenerator.popup.uuid.examiner.items

import com.github.f4b6a3.uuid.util.UuidUtil
import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.github.leomillon.uuidgenerator.popup.uuid.summarizeString
import org.intellij.lang.annotations.Language
import java.util.*
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings

/** All columns per line. */
data class ExaminationResult(
    val line: String,
    // Flatten list.
    var cells: List<CellInfo>,
    // Structured UUID-Information list.
    val uuidList: List<UuidInfos>?
) {
    fun countColumns(): Int {
        return cells.size
    }

    /** Skips all non-uuid entries and counts only the ones with UUIDs.
     * @return The `skipToEntry` UUID entry, not counting all non-UUID entries.
     * */
    fun uuidListEntry(skipToEntry: Int): UuidInfos? {
        if (uuidList.isNullOrEmpty()) return null
        var counter = 0
        for (uuidInfo in uuidList) {
            if (!uuidInfo.infos.isNullOrEmpty()) {
                if (counter == skipToEntry) {
                    return uuidInfo
                }
                counter++
            }
        }
        return null
    }
}

private enum class ItemType {
    /** Text part. */
    Text,

    /** UUID part. */
    UuidInfo,

    /** On one row it is text, on the other UUID. */
    Mixed
}

private data class ColumnCount(val source: UuidInfo, var columnCount: Int, val uuidIndex: Int) {
    constructor(source: UuidInfo, uuidIndex: Int) : this(source, source.columnCount, uuidIndex)
}

private data class SplitInfo(
    var itemType: ItemType, var totalCount: Int, var columnCounts: MutableList<ColumnCount>? = null, val uuidIndex: Int
) {
    constructor() : this(ItemType.Text, 1, null, 0)
}

/**
 * Header title spanning over multiple columns, grouping other columns below it.
 *
 * @param title Header title spanning over multiple columns.
 * @param start **Inclusive** start column.
 * @param stop **Exclusive** stop column.
 */
data class HeaderGroupTitle(val title: String, val start: Int, val stop: Int)

/**
 * Header Information for 2-row header,
 * with a top-title spanning over multiple columns and single column titles.
 */
data class HeaderInfo(val headerGroupTitles: List<HeaderGroupTitle>?, val singleTitles: List<String>)

@Language("HTML")
private const val htmlHead = """<head>
    <title>UUID examiner HTML report</title>
    <style>
        table {
            /* Merges double borders into single lines */
            border-collapse: collapse;
        }

        table, th, td {
            /* Applies borders to the table and every cell */
            border: 1px solid CanvasText;
            /* Adds space inside cells for readability */
            padding: 5px;
        }
    </style>
</head>
"""

data class BuildResult(val examinationResults: List<ExaminationResult>, val headerInfo: HeaderInfo) {

    /**
     * Render a Markdown formatted table of this objects content.
     *
     * The header comes only from [headerInfo.singleTitles], [HeaderInfo.headerGroupTitles]
     * is completely ignored, because Markdown can't handle 2-row tables.
     */
    fun convertToMarkdown(): String {
        val sb = StringBuilder()

        // Build header rows
        // Ignore first header row if it exists, Markdown doesn't support multi-row tables.

        // Second header row: single column captions
        sb.appendLine("| ${headerInfo.singleTitles.joinToString(" | ")} |")

        // Separator row
        sb.appendLine("| ${headerInfo.singleTitles.joinToString(" | ") { "---" }} |")

        val results = if (headerInfo.headerGroupTitles != null) {/* Skip 1st line, since it's the additional header line,
            which isn't supported by Markdown tables. */
            examinationResults.drop(1)
        } else {
            examinationResults
        }
        for (row in results) {
            sb.appendLine("| ${row.cells.joinToString(" | ") { it.value.toString() }} |")
        }
        return sb.toString()
    }

    /**
     * Render a HTML formatted table of this objects content.
     *
     * If [headerInfo.headerGroupTitles] is not null it draws a two-rows header, the following way:
     * - The 1st row with captions, which span a multi-column area for each entry of
     *      [headerInfo.headerGroupTitles], where the entry spans from column
     *      [HeaderGroupTitle.start] (inclusive) to [HeaderGroupTitle.stop] (exclusive),
     *      and the title is [HeaderGroupTitle.title].
     * - The 2nd row are normal single column captions from [HeaderInfo.singleTitles]
     *
     * If [headerInfo.headerGroupTitles] is null only the normal single column captions
     * from [HeaderInfo.singleTitles] are shown.
     *
     * The table content is from [examinationResults] (1st line is skipped
     * if [headerInfo.headerGroupTitles] is != null).
     */
    fun convertToHtml(): String {
        val sb = StringBuilder()
        sb.append(
            """<html lang="en">
$htmlHead
<body>
<table>
"""
        )

        // Header
        sb.appendLine("  <thead>")
        if (headerInfo.headerGroupTitles != null) {
            // 1st header row: multi-column spanning group titles
            sb.appendLine("    <tr>")
            var col = 0
            for (group in headerInfo.headerGroupTitles) {
                // Fill gap before this group with empty cells
                while (col < group.start) {
                    sb.appendLine("      <th></th>")
                    col++
                }
                val span = group.stop - group.start
                if (span > 1) {
                    sb.appendLine("      <th colspan=\"$span\">${group.title}</th>")
                } else {
                    sb.appendLine("      <th>${group.title}</th>")
                }
                col = group.stop
            }
            // Fill remaining columns
            while (col < headerInfo.singleTitles.size) {
                sb.appendLine("      <th></th>")
                col++
            }
            sb.appendLine("    </tr>")
        }
        // 2nd header row (or only row): single column captions
        sb.appendLine("    <tr>")
        for (title in headerInfo.singleTitles) {
            sb.appendLine("      <th>$title</th>")
        }
        sb.appendLine("    </tr>")
        sb.appendLine("  </thead>")

        // Body
        sb.appendLine("  <tbody>")
        val results = if (headerInfo.headerGroupTitles != null) {
            // Skip 1st line, since it's the additional header line
            examinationResults.drop(1)
        } else {
            examinationResults
        }
        for (row in results) {
            sb.appendLine("    <tr>")
            for (cell in row.cells) {
                sb.appendLine("      <td>${cell.value ?: ""}</td>")
            }
            sb.appendLine("    </tr>")
        }
        sb.appendLine("  </tbody>")

        sb.append(
            """</table>
</body>
</html>"""
        )
        return sb.toString()
    }
}

/**
 * Examine a single line.
 * @param line The line to parse.
 * @param parser The preconfigured CSV parser to use.
 */
private fun examineLine(
    line: String, parser: CsvParser?
): ExaminationResult {
    if (line.isBlank()) {
        return ExaminationResult(line, listOf(), listOf())
    }
    val split: Array<String?> = if (parser != null) {
        /* Regular expressions are too slow when examining large database excerpts.
        Another bonus: While double-quotes (") are respected as escaping the separator strings,
        the double quotes are automatically removed from the resulting segments. */
        parser.parseLine(line)
    } else {
        arrayOf(line)
    }
    val uuidList = mutableListOf<UuidInfos>()
    val cells = listOf<CellInfo>()
    for (lineEntry in split) {
        if (lineEntry == null) {
            continue
        }
        val uuidInfos: ArrayList<UuidInfo> = ArrayList<UuidInfo>()
        lineEntry.findUUIDs().forEach { (matchingValue, _) ->
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
                uuidInfos.add(UuidInfo(matchingValue, uuid, timestamp))
            }
        }
        if (uuidInfos.isNotEmpty()) {
            uuidList.add(UuidInfos(lineEntry, uuidInfos))
        } else {
            uuidList.add(UuidInfos(lineEntry, null))
        }
    }
    return ExaminationResult(line, cells, uuidList)
}

/** Find the maximum UUID entries per segment over all lines together.
 *  To fill it w/ empty cells for lines w/ fewer entries. */
private fun findColumnMaximums(examinedLines: List<ExaminationResult>): List<SplitInfo> {/*
    ExaminationResult:
        | split (UUIDv4 abcd UUIDv7) | split (defg) | split (UUIDv7) |
        | UuidInfos (UUIDv4 UUIDv7)                   |  UuidInfos (UUIDv7)  |
        | UuidInfo(UUIDv4) | UuidInfo(UUIDv7, Timestamp) | UuidInfo (UUIDv7, Timestamp) |
     */
    val splitInfos = mutableListOf<SplitInfo>()
    // First column is always just the plain text-source.
    splitInfos.add(SplitInfo())

    for (examinedLine in examinedLines) {
        if (examinedLine.uuidList?.isNotEmpty() == true) {
            // = 1 because the 1st column is always added as the source.
            var splitInfoIndex = 1
            for ((splitSegmentIndex, uuidInfos) in examinedLine.uuidList.withIndex()) {
                var size = 0
                if (uuidInfos.infos?.isNotEmpty() == true) {
                    val uuidColumnCount = mutableListOf<ColumnCount>()
                    for ((uuidInfoPerSplitSegmentIndex, uuidInfo) in uuidInfos.infos.withIndex()) {
                        uuidColumnCount.add(ColumnCount(uuidInfo, uuidInfoPerSplitSegmentIndex))
                        size += uuidInfo.columnCount
                    }
                    if (splitInfoIndex < splitInfos.size) {
                        val existing = splitInfos[splitInfoIndex]
                        if (existing.itemType == ItemType.Text) {
                            existing.itemType = ItemType.Mixed
                        }
                        existing.totalCount = maxOf(existing.totalCount, size)
                        if (existing.columnCounts != null) {
                            val existingSize = existing.columnCounts!!.size
                            // Set max columns per entry.
                            for (i in 0..<minOf(existingSize, uuidColumnCount.size)) {
                                existing.columnCounts!![i].columnCount =
                                    maxOf(existing.columnCounts!![i].columnCount, uuidColumnCount[i].columnCount)
                            }
                            // Add new columns.
                            if (uuidColumnCount.size > existingSize) {
                                for (i in existingSize..<uuidColumnCount.size) {
                                    existing.columnCounts?.add(uuidColumnCount[i])
                                }
                            }
                        } else {
                            existing.columnCounts = uuidColumnCount
                        }
                    } else {
                        splitInfos.add(
                            SplitInfo(
                                ItemType.UuidInfo, size, uuidColumnCount, splitSegmentIndex
                            )
                        )
                    }
                    splitInfoIndex++
                } else {
                    // Strange, has uuidList, but with no entries?!
                    //splitInfos.add(SplitInfo())
                }
            }
        } else {
            // Text without any UUIDs.
            splitInfos.add(SplitInfo())
        }
    }
    return splitInfos
}

private fun buildCellInfos(
    examinedLines: List<ExaminationResult>, splitInfos: List<SplitInfo>, summarizeSource: Boolean
) {
    for (examinedLine in examinedLines) {
        val cells = mutableListOf<CellInfo>()
        cells.add(
            CellInfo(
                ColumnType.String, if (summarizeSource) {
                    summarizeString(examinedLine.line)
                } else {
                    examinedLine.line
                }
            )
        )
        var uuidInfoSegmentIndex = 0
        for (splitInfoIndex in 1..<splitInfos.size) {
            val splitInfo = splitInfos[splitInfoIndex]
            if (splitInfo.itemType != ItemType.Text) {
                for ((splitInfoColumnIndex, uuidColumns) in splitInfo.columnCounts?.withIndex() ?: emptyList()) {
                    if (examinedLine.uuidList?.isNotEmpty() == true && examinedLine.uuidListEntry(uuidInfoSegmentIndex) != null) {
                        val uuidInfos: UuidInfos = examinedLine.uuidListEntry(uuidInfoSegmentIndex)!!
                        if (uuidInfos.infos?.isNotEmpty() == true && splitInfoColumnIndex < uuidInfos.infos.size) {
                            val uuidInfo = uuidInfos.infos[splitInfoColumnIndex]
                            cells.add(CellInfo(ColumnType.Uuid, uuidInfo.uuid))
                            cells.add(CellInfo(ColumnType.Version, uuidInfo.uuid.version()))
                            if (uuidInfo.timestamp != null) {
                                // If max columns reached, this must be identical.
                                if (uuidColumns.columnCount != uuidInfo.columnCount) {
                                    throw IllegalStateException(
                                        "Inconsistent column count for split index $splitInfoIndex, column index $splitInfoColumnIndex"
                                    )
                                }
                                cells.add(CellInfo(ColumnType.Timestamp, uuidInfo.timestamp))
                            } else if (uuidColumns.columnCount >= 3) {
                                cells.add(CellInfo(ColumnType.Timestamp, null))
                            }
                        } else {
                            // No data in this line, fill up with empty cells.
                            addCellInfos(cells, uuidColumns)
                        }
                        uuidInfoSegmentIndex++
                    } else {
                        // No data in this line, fill up with empty cells.
                        addCellInfos(cells, uuidColumns)
                    }
                }
            }
        }
        examinedLine.cells = cells
    }
}

/**
 * Add empty cells.
 */
private fun addCellInfos(
    cells: MutableList<CellInfo>, uuidColumns: ColumnCount
) {
    cells.add(CellInfo(ColumnType.Uuid, null))
    cells.add(CellInfo(ColumnType.Version, null))
    if (uuidColumns.columnCount == 3) {
        cells.add(CellInfo(ColumnType.Timestamp, null))
    }
}

private fun buildHeaderInfo(
    splitInfos: List<SplitInfo>, examinationResult: List<ExaminationResult>, hasHeader: Boolean
): HeaderInfo {
    val singleTitles = mutableListOf<String>()

    val headerGroupTitles: MutableList<HeaderGroupTitle>?
    val headerUuids: List<UuidInfos>?
    if (hasHeader && examinationResult.isNotEmpty() && examinationResult[0].uuidList?.isNotEmpty() == true) {
        headerGroupTitles = mutableListOf()
        headerUuids = examinationResult[0].uuidList
    } else {
        headerGroupTitles = null
        headerUuids = null
    }
    // First column is always the source text, no need to analyze here.
    singleTitles.add("Source")
    headerGroupTitles?.add(HeaderGroupTitle("Source", 0, 1))
    var headerTopTitleIndex = 1
    for (splitInfoIndex in 1..<splitInfos.size) {
        val splitInfo: SplitInfo = splitInfos[splitInfoIndex]
        if (splitInfo.itemType == ItemType.Text) {
            // TODO: Currently not entered branch, non-uuid columns should be skipped?!
            singleTitles.add("Text")
            if (headerGroupTitles != null && headerUuids != null && splitInfo.uuidIndex < headerUuids.size) {
                headerGroupTitles.add(
                    HeaderGroupTitle(
                        headerUuids[splitInfo.uuidIndex].lineEntry, headerTopTitleIndex++, headerTopTitleIndex
                    )
                )
            }
            headerTopTitleIndex++
        } else if (splitInfo.columnCounts != null) {
            var addIndex = 0
            for (columnCount in splitInfo.columnCounts!!) {
                singleTitles.add("UUID")
                singleTitles.add("Version")
                if (columnCount.columnCount == 3) {
                    singleTitles.add("Timestamp")
                    addIndex += 3
                } else {
                    addIndex += 2
                }
            }
            if (headerGroupTitles != null && headerUuids != null && splitInfo.uuidIndex < headerUuids.size) {
                headerGroupTitles.add(
                    HeaderGroupTitle(
                        headerUuids[splitInfo.uuidIndex].lineEntry, headerTopTitleIndex, headerTopTitleIndex + addIndex
                    )
                )
                headerTopTitleIndex += addIndex
            }
        }
    }
    return HeaderInfo(headerGroupTitles, singleTitles)
}

/**
 * Convert text to JTable usable data.
 *
 * @param trimWhitespace Trim the individual line segments / splits.
 *          The whole lines are always trimmed at the beginning and end.
 */
fun convertInputToTableLines(
    input: String,
    separatingChars: String?,
    trimWhitespace: Boolean,
    hasHeader: Boolean,
    hasHeaderSeparator: Boolean,
    summarizeSource: Boolean
): BuildResult {
    /* Parser to make this:
            a,"b,c","d""e,f",g
        parse correctly.

        So that the separator (like a "," or a "|"), enclosed in double-quotes ("),
        doesn't count as split instruction. Also surrounding quotes are stripped from
        the result. */
    val format: CsvParser? = if (separatingChars?.isNotEmpty() == true) {
        val settings = CsvParserSettings()
        settings.format.setDelimiter(separatingChars)
        settings.format.quote = '\"'
        settings.ignoreLeadingWhitespaces = trimWhitespace
        settings.ignoreTrailingWhitespaces = trimWhitespace
        // Don't fail on unescaped/malformed quotes
        settings.keepQuotes = false
        settings.isQuoteDetectionEnabled = false
        CsvParser(settings)
    } else {
        null
    }
    var lines = input.lines()
    if (lines.size >= 2 && hasHeaderSeparator) {
        // Remove header separator at index 1
        lines = lines.toMutableList().also { it.removeAt(1) }
    }
    val examinedLines = lines
        .map { line -> examineLine(line.trim(), format) }
    val splitInfos = findColumnMaximums(examinedLines)

    buildCellInfos(examinedLines, splitInfos, summarizeSource)
    val headerInfo = buildHeaderInfo(
        splitInfos, examinedLines,
        // Ignore header request when there's only 1 line.
        hasHeader && examinedLines.size > 1
    )
    return BuildResult(examinedLines, headerInfo)
}
