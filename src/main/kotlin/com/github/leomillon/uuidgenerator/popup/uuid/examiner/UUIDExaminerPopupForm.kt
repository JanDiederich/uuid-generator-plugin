package com.github.leomillon.uuidgenerator.popup.uuid

import com.github.leomillon.uuidgenerator.parser.findUUIDs
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.CustomHeaderRenderer
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.UUIDExaminerPopupSettings
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.BuildResult
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.ExaminationResult
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.HeaderInfo
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.convertInputToTableLines
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import java.awt.AWTEvent
import java.awt.ItemSelectable
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.event.HierarchyEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel
import javax.swing.text.JTextComponent
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants


/** Maximum width of auto-sizing first column with the source. */
private const val SourceColumnMaxWidth = 200

fun summarizeString(matchingValue: String): String {
    // Max prefix and suffix length.
    val partsLength = 8

    val endsLength = matchingValue.length / 2
    // Make partsLength not overlap or go out of bounds.
    val preSuffixLength = minOf(endsLength, partsLength)
    return if (preSuffixLength > 0) {
        matchingValue.substring(
            0, preSuffixLength
        ) + "..." + matchingValue.substring(matchingValue.length - preSuffixLength)
    } else {
        matchingValue
    }
}

class UUIDExaminerPopupForm {
    var buildResult: BuildResult? = null
    private val logger = Logger.getInstance(UUIDExaminerPopupForm::class.java)

    private class ExaminationTableModel : TableModel {
        val examinationResult: List<ExaminationResult>
        val headerInfo: HeaderInfo
        val columnsCount: Int

        constructor(examinationResults: List<ExaminationResult>, headerInfo: HeaderInfo) {
            this.examinationResult = examinationResults
            this.headerInfo = headerInfo
            // All lines have the same amount of columns.
            this.columnsCount = if (examinationResults.isNotEmpty()) {
                examinationResults.first().cells.size
            } else {
                0
            }
        }

        override fun getRowCount(): Int {
            return if (headerInfo.headerGroupTitles == null) {
                examinationResult.size
            } else {
                maxOf(examinationResult.size - 1, 0)
            }
        }

        override fun getColumnCount(): Int {
            return columnsCount
        }

        override fun getColumnName(columnIndex: Int): String? {
            return if (columnIndex < headerInfo.singleTitles.size) {
                headerInfo.singleTitles[columnIndex]
            } else {
                null
            }
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return String::class.java
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            return false
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
            return if (columnIndex < examinationResult[rowIndex].cells.size) {
                val rowIndexIncHeader = if (headerInfo.headerGroupTitles != null) {
                    rowIndex + 1
                } else {
                    rowIndex
                }
                examinationResult[rowIndexIncHeader].cells[columnIndex].value.toString()
            } else {
                null
            }
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            // Not editable.
        }

        override fun addTableModelListener(l: TableModelListener?) {
            // Not editable.
        }

        override fun removeTableModelListener(l: TableModelListener?) {
            // Not editable.
        }
    }

    var panel: JPanel? = null

    var inputTextPane: JTextPane? = null
    var examinationTable: JTable? = null
    var separatingChars: JTextField? = null
    var trimWhitespaceCheck: JCheckBox? = null
    var headerCheck: JCheckBox? = null
    var headerSeparatorCheck: JCheckBox? = null
    var summarizeCheck: JCheckBox? = null

    private val settings: UUIDExaminerPopupSettings = UUIDExaminerPopupSettings.instance

    fun component(): JComponent? = panel

    init {
        loadSettings()
        examineInput()

        sequenceOf(
            inputTextPane, separatingChars
        ).filterNotNull().forEach { textComponent: JTextComponent? ->
            textComponent?.document?.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = updateContentAndSave()
                override fun removeUpdate(e: DocumentEvent?) = updateContentAndSave()
                override fun changedUpdate(e: DocumentEvent?) {
                    // Ignore all format changes.
                }
            })
        }

        sequenceOf<ItemSelectable?>(
            headerCheck, headerSeparatorCheck, trimWhitespaceCheck, summarizeCheck
        ).filterNotNull().forEach { uiComponent ->
            uiComponent.addItemListener {
                updateContentAndSave()
            }
        }

        examinationTable?.autoCreateRowSorter = true
        oneLineBugWorkaround()
    }

    /**
     * ========= Workaround for known IntelliJ Bug ============
     *
     * That bug removes all newlines from any pasted text into anything which isn't a JTextArea.
     *
     * [Source](https://youtrack.jetbrains.com/issue/IJPL-161783/Paste-into-non-JTextArea-lost-newlines)
     *
     * [Look at](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/editor/textarea/TextComponentEditorImpl.java)
     * ```
     * public boolean isOneLineMode()
     * ```
     * , if that is still
     * ```
     * !(myTextComponent instanceof JTextArea)
     * ```
     * ,
     * then this workaround is still needed.
     *
     * ========================================================
     */
    private fun oneLineBugWorkaround() {
        // IntelliJ's IdeEventQueue intercepts Ctrl+V before Swing's TransferHandler,
        // paste() or DocumentFilter ever see it – and strips newlines in the process.
        // The only reliable fix is to register our own dispatcher that runs *before*
        // IntelliJ's action system and reads the raw clipboard text ourselves.
        val pasteDisposable = Disposer.newDisposable("UUIDExaminerPopupForm-paste")
        panel?.addHierarchyListener { e ->
            if ((e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong()) != 0L && panel?.isShowing == false) {
                Disposer.dispose(pasteDisposable)
            }
        }
        IdeEventQueue.getInstance().addDispatcher(
            object : IdeEventQueue.NonLockedEventDispatcher {
                override fun dispatch(e: AWTEvent): Boolean {
                    return if (e is KeyEvent && e.id == KeyEvent.KEY_PRESSED //
                        && ((e.keyCode == KeyEvent.VK_V) && (e.isControlDown || e.isMetaDown) //
                                || (e.keyCode == KeyEvent.VK_INSERT && e.isShiftDown)) //
                        && inputTextPane?.isFocusOwner == true
                    ) {
                        pasteToInputPane()
                        true // consume – prevents IntelliJ's paste action from running
                    } else {
                        false
                    }
                }
            }, pasteDisposable
        )
    }

    /**
     * Updates the examination and immediately save the current settings.
     * It's assumed that the "Copy to clipboard" gets used rarely.
     */
    private fun updateContentAndSave() {
        examineInput()
        applyToSettings(settings)
    }

    /**
     * Reads plain text (including newlines) directly from the system clipboard and inserts it
     * at the current caret position, replacing any active selection.
     *
     * This is needed because IntelliJ's IdeEventQueue intercepts Ctrl+V before Swing components
     * receive it, and the built-in $Paste action strips newlines for JTextPane.
     *
     * @see [oneLineBugWorkaround]
     */
    private fun pasteToInputPane() {
        val text = try {
            Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as? String
        } catch (e: Exception) {
            logger.warn("Failed to read clipboard content for paste")
            throw e
        } ?: return

        val pane = inputTextPane ?: return
        val doc = pane.document
        try {
            val selStart = pane.selectionStart
            val selEnd = pane.selectionEnd
            if (selStart != selEnd) {
                doc.remove(selStart, selEnd - selStart)
            }
            doc.insertString(pane.caretPosition, text, null)
        } catch (e: Exception) {
            logger.warn("Failed to insert pasted text into input pane")
            throw e
        }
    }

    fun clearAllFormatting() {
        val doc = inputTextPane?.getStyledDocument()
        val blank = SimpleAttributeSet()

        // Clear all attributes
        doc?.setCharacterAttributes(0, doc.length, blank, true)
    }

    fun highlightUuidRanges(input: String) {
        // Reset
        clearAllFormatting()

        // Get the StyledDocument
        val doc = inputTextPane?.getStyledDocument()

        // Create the highlight style.
        val uuidFoundStyle = SimpleAttributeSet()
        StyleConstants.setBold(uuidFoundStyle, true)
        StyleConstants.setUnderline(uuidFoundStyle, true)

        input.findUUIDs().forEach { (_, range) ->
            val length = range.last + 1 - range.first

            /* Apply highlight style. The 'false' parameter means it merges with existing styles (like color)
            instead of replacing everything. */
            doc?.setCharacterAttributes(range.first, length, uuidFoundStyle, false)
        }
    }

    private fun examineInput() {
        val input: String = inputTextPane?.text ?: ""
        val separatingChars = if (separatingChars?.text?.isNotEmpty() == true) separatingChars?.text else null
        val trimWhitespace = trimWhitespaceCheck?.isSelected == true
        val hasHeader = headerCheck?.isSelected == true
        val hasHeaderSeparator = headerSeparatorCheck?.isSelected == true
        val summarizeSource = summarizeCheck?.isSelected == true

        val buildResult = convertInputToTableLines(
            input, separatingChars, trimWhitespace, hasHeader, hasHeaderSeparator, summarizeSource
        )
        this.buildResult = buildResult

        /* This update was triggered by a user-edit. So the document is currently locked,
        *  and the formatting change must be done in the next UI loop. */
        SwingUtilities.invokeLater {
            highlightUuidRanges(input)
        }

        val examinationResults = buildResult.examinationResults
        examinationResults.let { examinationResult ->
            val model = ExaminationTableModel(
                examinationResult, buildResult.headerInfo
            )
            examinationTable?.tableHeader?.defaultRenderer = CustomHeaderRenderer(buildResult.headerInfo)
            examinationTable?.model = model
            // Autosize all columns to fit their content.
            examinationTable?.let { table ->
                for (col in 0 until table.columnCount) {
                    var maxWidth = 0
                    // Header width
                    val headerRenderer = table.tableHeader?.defaultRenderer
                    val headerComp = headerRenderer?.getTableCellRendererComponent(
                        table, table.getColumnName(col), false, false, -1, col
                    )
                    if (headerComp != null) maxWidth = headerComp.preferredSize.width
                    // Cell widths
                    for (row in 0 until table.rowCount) {
                        val renderer = table.getCellRenderer(row, col)
                        val comp = table.prepareRenderer(renderer, row, col)
                        maxWidth = maxOf(maxWidth, comp.preferredSize.width)
                    }
                    if (col == 0) {
                        maxWidth = minOf(maxWidth, SourceColumnMaxWidth)
                    }
                    table.columnModel.getColumn(col).preferredWidth = maxWidth + 10
                }
                table.autoResizeMode = JTable.AUTO_RESIZE_OFF
            }
        }
    }

    private fun loadSettings() {
        // Don't overwrite example text with empty settings.
        if (settings.inputText != null) {
            inputTextPane?.text = settings.inputText
        }
        separatingChars?.text = settings.separatingChars
        trimWhitespaceCheck?.isSelected = settings.trimeWhitespace
        headerCheck?.isSelected = settings.hasHeader
        headerSeparatorCheck?.isSelected = settings.hasHeaderSeparator
        summarizeCheck?.isSelected = settings.summarizeCheck
    }

    fun applyToSettings(settings: UUIDExaminerPopupSettings) {
        settings.inputText = inputTextPane?.text
        settings.separatingChars = separatingChars?.text ?: ""
        settings.trimeWhitespace = trimWhitespaceCheck?.isSelected == true
        settings.hasHeader = headerCheck?.isSelected == true
        settings.hasHeaderSeparator = headerSeparatorCheck?.isSelected == true
        settings.summarizeCheck = summarizeCheck?.isSelected == true
    }

    /**
     * See [BuildResult.convertToMarkdown].
     */
    fun buildMarkdownOutput(): String? {
        return buildResult?.convertToMarkdown()
    }

    /**
     * See [BuildResult.convertToHtml].
     */
    fun buildHtmlOutput(): String? {
        return buildResult?.convertToHtml()
    }
}
