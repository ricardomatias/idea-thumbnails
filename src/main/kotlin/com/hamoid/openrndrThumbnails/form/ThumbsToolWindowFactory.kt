package com.hamoid.openrndrThumbnails.form

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class ThumbsToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val thumbnailsPanel = ThumbsPanel(project)
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(thumbnailsPanel, null, false)
        contentManager.addContent(content)
    }
}