package com.hamoid.openrndrThumbnails.form

import com.intellij.ui.JBColor
import java.awt.Component
import javax.swing.*

/**
 * Changes the cell rendering style depending on if it is selected or not.
 */
class CellStyle : ListCellRenderer<Any> {
    override fun getListCellRendererComponent(
        list: JList<out Any>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component =
        if (value is JPanel) {
            value.components.forEach {
                it.foreground = if (isSelected) {
                    UIManager.getColor("List.selectionForeground")
                } else {
                    JBColor.foreground()
                }
            }
            value.background = if (isSelected)
                UIManager.getColor("List.selectionBackground")
            else
                JBColor.background()
            value
        }
        else JLabel("")
}