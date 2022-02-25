package com.hamoid.openrndrThumbnails.utils

import java.awt.Image
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon

class IconUtils {
    companion object {
        fun createSmallIcon(iconFilePath: String) =
            createIcon(iconFilePath, 100)

        fun createOriginalIcon(iconFilePath: String) =
            createIcon(iconFilePath)

        private fun createIcon(iconFilePath: String, width: Int = 0) = try {
            val img = ImageIO.read(File(iconFilePath))
            ImageIcon(
                if (width > 0) {
                    val height = width * img.height / img.width
                    img.getScaledInstance(width, height, Image.SCALE_FAST)
                } else {
                    img
                }
            )
        } catch (e: IOException) {
            null // TODO return placeholder icon
        }
    }
}
