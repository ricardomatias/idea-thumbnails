package com.github.hamoid.ideathumbnails

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.layout.panel
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel


class ThumbnailsTool : ToolWindowFactory {
    private val img = ImageIO.read(File("/tmp/untitled/media/b.png"))
    private val ico = ImageIcon(img)
    private val lab = JLabel()

    init {
        lab.icon = ico
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(
            makeUI(), null, false
        )
        contentManager.addContent(content)
    }

    private fun makeUI() = panel {
        row {
            label("hello")
            button("do it") {
                println("done")
            }
        }
        row {
            label("Image goes here")
            // FIXME: The next image is not visible
            // How to show images?

            lab
        }
    }
}