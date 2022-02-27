package com.hamoid.openrndrThumbnails.utils

import java.awt.Image
import java.awt.image.BufferedImage
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

        fun createIcon(iconFilePath: String, width: Int = 0): ImageIcon {
            val file = File(iconFilePath)

            if (!file.exists()) {
                return emptyIcon()
            }

            return try {
                val img = ImageIO.read(file)

                if (width > 0) {
                    ImageIcon(
                        img.getScaledInstance(
                            width, width * img.height / img.width,
                            Image.SCALE_SMOOTH
                        )
                    )
                } else {
                    ImageIcon(img)
                }


            } catch (e: IOException) {
                emptyIcon()
            }
        }

        private fun emptyIcon() = ImageIcon(
            BufferedImage(100, 16, BufferedImage.TYPE_INT_ARGB)
        )

        fun ImageIcon.save(iconFilePath: String) {
            val img = BufferedImage(
                iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB
            )
            img.createGraphics().also {
                it.drawImage(this.image, 0, 0, null)
                it.dispose()
            }
            ImageIO.write(img, "png", File(iconFilePath))
        }
    }
}
