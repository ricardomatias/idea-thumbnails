package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.io.InputStreamReader
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
        val panel = JPanel(BorderLayout())

        val img = JLabel().apply {
            icon = kotlinFile.panel.getThumbLarge()
            iconTextGap = 0
        }

        val date = InputStreamReader(
            Runtime.getRuntime().exec(
                listOf(
                    "sh", "-c", "git log " +
                            "--diff-filter=A --follow " +
                            "--format='%as (%ar)' -- " +
                            "'${kotlinFile.relativePath}' | tail -1"
                ).toTypedArray(),
                arrayOf(), KotlinFile.root
            ).inputStream
        ).readText()

        panel.add(img, BorderLayout.CENTER)
        panel.add(
            JLabel(
                "<html>${kotlinFile.description}<br>$date<br>${kotlinFile.tags}</html>"
            ), BorderLayout.SOUTH
        )

        return panel
    }

    override fun createActions() = arrayOf(
        DialogWrapperExitAction("OK", OK_EXIT_CODE)
    )
}