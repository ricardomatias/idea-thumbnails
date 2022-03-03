package com.hamoid.openrndrThumbnails.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon


class IconUtils {
    companion object {
        fun createSmallIcon(path: String) =
            createIcon(path, 100)

        fun createIcon(path: String, width: Int = 0): ImageIcon {
            val img = loadImage(path)
            return ImageIcon(if (width > 0) img.scaled(width) else img)
        }

        fun deleteIcon(path: String) = File(path).delete()

        fun loadImage(path: String): Image {
            val file = File(path)

            if (!file.exists()) {
                return emptyImage()
            }

            return try {
                ImageIO.read(file)
            } catch (e: IOException) {
                emptyImage()
            }

        }

        fun emptyImage() = BufferedImage(
            100, 16, BufferedImage.TYPE_INT_ARGB
        )

        private fun Image.toBuffered() = BufferedImage(
            getWidth(null),
            getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        ).apply {
            createGraphics().also {
                it.drawImage(this@toBuffered, 0, 0, null)
                it.dispose()
            }
        }

        fun Image.save(path: String) =
            ImageIO.write(this.toBuffered(), "png", File(path))

        fun Image.scaled(width: Int): Image = this.getScaledInstance(
            width, width * getHeight(null) / getWidth(null),
            Image.SCALE_SMOOTH
        )
    }
}
