package com.hamoid.openrndrThumbnails.form

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * factoryClass
 *
 * Called by plugin.xml
 * Everything starts here.
 */
class ThumbsToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, win: ToolWindow) {
        win.contentManager.apply {
            addContent(
                factory.createContent(ThumbsPanel(project), null, false)
            )
        }
    }
}
