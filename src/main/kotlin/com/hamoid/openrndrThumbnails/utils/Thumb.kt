package com.hamoid.openrndrThumbnails.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon

class Thumb(private val thumbPathLarge: String) {
    private val thumbPathSmall = thumbPathLarge.replace(
        ".png", "_small.png"
    )

    val small = ImageIcon(emptyImage)
    val large get() = ImageIcon(loadIcon(thumbPathLarge, WIDTH_LARGE))

    companion object {
        private const val WIDTH_SMALL = 100
        private const val HEIGHT_SMALL = 16
        private const val WIDTH_LARGE = 640
        private val emptyImage = BufferedImage(
            WIDTH_SMALL,
            HEIGHT_SMALL,
            BufferedImage.TYPE_INT_ARGB
        )
    }

    /**
     * Try load the [small] icon from
     * the corresponding path. Set image to null if not found.
     */
    fun load() {
        small.image = loadIcon(thumbPathSmall, WIDTH_SMALL)
    }

    /**
     * Returns an [ImageIcon] loaded from [path] and
     * scaled to [maxWidth].
     */
    private fun loadIcon(path: String, maxWidth: Int): Image {
        val f = File(path)
        if (f.exists()) {
            try {
                val img = ImageIO.read(f)
                return if (img.getWidth(null) > maxWidth)
                    img.scaled(maxWidth)
                else
                    img
            } finally {
            }
        }
        return emptyImage
    }

    /**
     * Check if the icons not set
     */
    fun isEmpty() = small.image == emptyImage

    /**
     * Delete thumbnails, reset [small] image.
     */
    fun delete() {
        val fSmall = File(thumbPathSmall)
        val fLarge = File(thumbPathLarge)
        fSmall.delete()
        fLarge.delete()
        if (!fSmall.exists()) {
            small.image = emptyImage
        }
    }

    /**
     * Load [Image] from [sourcePath] and save it
     * to disk.
     */
    fun setFrom(sourcePath: String) {
        val f = File(sourcePath)
        if (!f.exists()) {
            return
        }
        try {
            set(ImageIO.read(f))
        } catch (_: Exception) {
        }
    }

    /**
     * Save [sourceImage] scaled to [thumbPathSmall] and
     * [thumbPathLarge]
     */
    fun set(sourceImage: Image) {
        sourceImage.scaled(WIDTH_LARGE).save(thumbPathLarge)
        sourceImage.scaled(WIDTH_SMALL).save(thumbPathSmall)
        load()
    }

    /**
     * Saves an image to disk in PNG format
     */
    private fun Image.save(path: String) =
        ImageIO.write(this.toBuffered(), "png", File(path))

    /**
     * Returns an image scaled proportionally to the
     * requested [width] value.
     */
    private fun Image.scaled(width: Int): Image = this.getScaledInstance(
        width, width * getHeight(null) / getWidth(null),
        Image.SCALE_SMOOTH
    )

    /**
     * Convert an [Image] into a [BufferedImage] so it
     * can be saved to disk.
     */
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
}
