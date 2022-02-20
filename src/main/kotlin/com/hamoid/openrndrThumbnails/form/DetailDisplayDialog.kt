package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.model.DrawableModel
import com.hamoid.openrndrThumbnails.utils.IconUtils
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.ScrollPaneConstants.*
import javax.swing.border.SoftBevelBorder

class DetailDisplayDialog(project: Project, drawableModel: DrawableModel) :
    DialogWrapper(project, true) {

    private var mainPanel: JPanel? = null
    private val subPanel: JPanel = JPanel()
    private var springLayout: SpringLayout? = null

    init {
        springLayout = SpringLayout()

        subPanel.run {
            layout = springLayout
        }

        isAutoAdjustable = false
        setResizable(true)
        setSize(480, 360)

        title = drawableModel.fileName

        createContent(drawableModel)
        init()
    }

    private fun createContent(model: DrawableModel) {
        addDisplayImage(model)
    }

    // todo: make it crawlable
    private fun addDisplayImage(
        model: DrawableModel
    ) {
        var oldPanel: JPanel? = null
        var panelWidth = HORIZONTAL_PADDING
        var panelHeight = 0

        mainPanel?.add(createScrollPane(), BorderLayout.CENTER)

        model.filePathList.forEach { filePath ->

            val iconLabel = JLabel().apply {
                icon = IconUtils.createOriginalIcon(filePath)
                border = SoftBevelBorder(SoftBevelBorder.LOWERED)
            }

            val panel = JPanel().apply {
                layout = BorderLayout()
                add(iconLabel, BorderLayout.CENTER)
            }

            updateContainer(panel, oldPanel)

            panelWidth += panel.width + HORIZONTAL_PADDING
            if (panelHeight < panel.height) {
                panelHeight = panel.height
            }

            oldPanel = panel
        }

        setContainerSize(panelWidth, panelHeight)
    }

    private fun createScrollPane() =
        JBScrollPane(
            VERTICAL_SCROLLBAR_AS_NEEDED,
            HORIZONTAL_SCROLLBAR_AS_NEEDED
        ).apply {
            setViewportView(subPanel)
        }

    private fun updateContainer(newPanel: JPanel, oldPanel: JPanel?) {
        val layout = if (oldPanel == null) SpringLayout.WEST else SpringLayout.EAST
        val panel = oldPanel ?: subPanel
        springLayout?.run {
            putConstraint(
                SpringLayout.NORTH, newPanel, VERTICAL_PADDING,
                SpringLayout.NORTH, subPanel
            )
            putConstraint(
                SpringLayout.WEST, newPanel, HORIZONTAL_PADDING,
                layout, panel
            )
        }
        subPanel.add(newPanel)
        subPanel.doLayout()
    }

    private fun setContainerSize(width: Int, height: Int) {
        subPanel.preferredSize = Dimension(width, height + VERTICAL_PADDING * 2)
    }

    override fun createCenterPanel(): JComponent? = mainPanel

    override fun createActions(): Array<Action> =
        Array(1) { DialogWrapperExitAction("OK", OK_EXIT_CODE) }

    companion object {
        private const val VERTICAL_PADDING = 8
        private const val HORIZONTAL_PADDING = 16
    }
}