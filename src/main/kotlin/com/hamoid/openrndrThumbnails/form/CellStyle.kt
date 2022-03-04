package com.hamoid.openrndrThumbnails.form

import com.intellij.ui.JBColor
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

/**
 * Changes the cell rendering style depending on if it is selected or not.
 */
class CellStyle : ListCellRenderer<Any> {
    override fun getListCellRendererComponent(
        list: JList<out Any>?, value: Any?, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean
    ): Component = (value as Component).apply {
        if (isSelected) {
            foreground = JBColor.white
            background = JBColor.blue
        } else {
            foreground = JBColor.foreground()
            background = JBColor.background()
        }
    }
}