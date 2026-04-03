package com.github.leomillon.uuidgenerator.popup.uuid.examiner

import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.HeaderGroupTitle
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.HeaderInfo
import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.*
import javax.swing.SwingConstants.CENTER
import javax.swing.table.DefaultTableCellRenderer

private fun backgroundColor(): Color? = UIManager.getColor("TableHeader.background")
private fun foregroundColor(): Color? = UIManager.getColor("TableHeader.foreground")

private fun defaultHeaderLabel(text: String): JLabel {
    val label = JLabel(text, CENTER)
    label.font = label.font.deriveFont(Font.PLAIN)
    label.border = BorderFactory.createLineBorder(foregroundColor())
    label.isOpaque = true
    label.background = backgroundColor()
    label.foreground = foregroundColor()
    return label
}

/**
 * Draws a custom `JTable` header.
 *
 * If [headerInfo.headerTitles] is not null it draws a two-rows header, the following way:
 * - The 1st row with captions, which span a multi-column area for each entry of `headerTitles`,
 *      where the entry spans from column [HeaderTitle.start] (inclusive)
 *      to [HeaderTitle.stop] (exclusive), and the title is [HeaderTitle.title].
 * - The 2nd row are normal single column captions from [HeaderInfo.singleTitles]
 *
 * If [headerInfo.headerTitles] is null only the normal single column captions
 * from [HeaderInfo.singleTitles] are shown.
 */
class CustomHeaderRenderer(var headerInfo: HeaderInfo) : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        val headerTitles = headerInfo.headerGroupTitles
        val singleTitles = headerInfo.singleTitles

        if (headerTitles == null) {
            return defaultHeaderLabel(singleTitles.getOrElse(column) { "" })
        }

        // Satisfy IntelliJ warnings.
        accessibleContext?.accessibleName = "2-Row Header"

        return TwoRowHeaderPanel(column, headerTitles, singleTitles, table)
    }

    private class TwoRowHeaderPanel(
        private val column: Int,
        headerGroupTitles: List<HeaderGroupTitle>,
        singleTitles: List<String>,
        private val table: JTable
    ) : JPanel(BorderLayout()) {

        private val groupTitle: HeaderGroupTitle? =
            headerGroupTitles.firstOrNull { column >= it.start && column < it.stop }
        private val topRowHeight = 25

        init {
            background = backgroundColor()
            foreground = foregroundColor()

            // Top row is a spacer – actual group title is painted manually in paintChildren
            val topSpacer = JPanel()
            topSpacer.isOpaque = false
            topSpacer.preferredSize = Dimension(0, topRowHeight)
            add(topSpacer, BorderLayout.NORTH)

            // 2nd row: single column title
            val bottomText = singleTitles.getOrElse(column) { "" }

            val bottomLabel = defaultHeaderLabel(bottomText)

            add(bottomLabel, BorderLayout.CENTER)
        }

        override fun paintChildren(g: Graphics) {
            super.paintChildren(g)

            val gt = groupTitle ?: return
            val g2d = g.create() as Graphics2D
            g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )

            val columnModel = table.columnModel

            // Compute the x-offset of the span start relative to this cell's left edge
            var spanStartX = 0
            for (c in gt.start until column) {
                spanStartX -= columnModel.getColumn(c).width
            }

            // Compute total span width
            var spanWidth = 0
            for (c in gt.start until gt.stop) {
                spanWidth += columnModel.getColumn(c).width
            }

            g2d.color = foregroundColor() ?: JBColor.GRAY
            // Draw separator line at top of the top row
            g2d.drawLine(spanStartX, 1, spanStartX + spanWidth - 1, 1)
            // Bottom horizontal separator already drawn by the 2nd row top line border.

            // Draw vertical borders at span edges (left only on first col, right only on last col)
            // Left
            if (column == gt.start) {
                g2d.drawLine(spanStartX, 0, spanStartX, topRowHeight - 1)
            }
            // Right
            if (column == gt.stop - 1) {
                g2d.drawLine(spanStartX + spanWidth - 1, 0, spanStartX + spanWidth - 1, topRowHeight - 1)
            }

            // Draw centered title across the full span
            val font = this.font.deriveFont(Font.PLAIN)
            g2d.font = font
            g2d.color = foregroundColor() ?: JBColor.BLACK
            val fontMetrics = g2d.fontMetrics
            val textWidth = fontMetrics.stringWidth(gt.title)
            val textX = spanStartX + (spanWidth - textWidth) / 2
            val textY = (topRowHeight + fontMetrics.ascent - fontMetrics.descent) / 2
            g2d.drawString(gt.title, textX, textY)

            g2d.dispose()
        }
    }
}