package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.IconUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class OriginalImageDialog(
    project: Project,
    private val kotlinFile: KotlinFile
) :
    DialogWrapper(project, true) {

    init {
        isAutoAdjustable = true
        title = kotlinFile.relativePath
        setSize(640, 480)
        init()
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())

        val icon = JLabel().apply {
            icon = IconUtils.createIcon(kotlinFile.thumbPath)
            iconTextGap = 0
        }
        dialogPanel.add(icon, BorderLayout.CENTER)

        return dialogPanel
    }

    override fun createActions() = arrayOf(
        DialogWrapperExitAction("OK", OK_EXIT_CODE)
    )
}