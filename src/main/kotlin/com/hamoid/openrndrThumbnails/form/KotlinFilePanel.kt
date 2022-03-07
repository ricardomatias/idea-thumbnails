package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.Thumb
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Image
import javax.swing.JLabel
import javax.swing.border.EmptyBorder

/**
 * A simple component that contains a label with a thumbnail
 * and a text
 */
class KotlinFilePanel(private val kotlinFile: KotlinFile) : JLabel() {
    private val thumb = Thumb(kotlinFile.thumbPath)

    init {
        font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
        iconTextGap = 10
        horizontalAlignment = LEFT
        layout = FlowLayout().apply {
            alignment = FlowLayout.LEFT
        }
        border = EmptyBorder(2, 2, 2, 2)
        icon = thumb.small
        text = kotlinFile.relativePath
            .split("/")
            .joinToString("<br>", "<html>", "</html>")

        thumb.load()
    }

    /**
     * Differentiate click on text vs click on image
     */
    fun isTextClick(mouseX: Int) = mouseX > icon.iconWidth +
            iconTextGap / 2

    fun setThumb(path: String) = thumb.setFrom(path)
    fun setThumb(img: Image) = thumb.set(img)
    fun getThumbLarge() = thumb.large
    fun clearThumb() = thumb.delete()
    fun hasNoThumb() = thumb.isEmpty()
}