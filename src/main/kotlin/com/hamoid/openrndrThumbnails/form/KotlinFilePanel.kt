package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.IconUtils
import com.hamoid.openrndrThumbnails.utils.IconUtils.Companion.save
import com.hamoid.openrndrThumbnails.utils.IconUtils.Companion.scaled
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Image
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * A simple component that contains a label with a thumbnail
 * and a text
 */
class KotlinFilePanel(private val kotlinFile: KotlinFile) : JLabel() {
    init {
        font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
        iconTextGap = 10
        horizontalAlignment = LEFT
        layout = FlowLayout().apply {
            alignment = FlowLayout.LEFT
        }
        border = EmptyBorder(2, 2, 2, 2)
    }

    var largeThumb: Image
        get() = IconUtils.createIcon(kotlinFile.thumbPath).image
        set(img) = saveImage(img)

    /**
     * Update component with new text and new thumbnail
     */
    fun rebuild() {
        text = kotlinFile.relativePath
            .split("/")
            .joinToString("<br>", "<html>", "</html>")

        icon = IconUtils.createSmallIcon(kotlinFile.thumbPath)
    }

    private fun saveImage(img: Image) {
        if (img.getWidth(null) > 640) {
            img.scaled(640).save(kotlinFile.thumbPath)
        } else {
            img.save(kotlinFile.thumbPath)
        }
        icon = IconUtils.createSmallIcon(kotlinFile.thumbPath)
    }

    fun clear() {
        if (IconUtils.deleteIcon(kotlinFile.thumbPath)) {
            icon = ImageIcon(IconUtils.emptyImage())
        }
    }

    fun isTextClick(mouseX: Int) = mouseX > icon.iconWidth +
            iconTextGap / 2

    fun isEmptyIcon() = IconUtils.isEmpty(icon)
}