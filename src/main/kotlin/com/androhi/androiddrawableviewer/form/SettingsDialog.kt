package com.androhi.androiddrawableviewer.form

import com.androhi.androiddrawableviewer.Constants
import com.androhi.androiddrawableviewer.PluginConfig
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class SettingsDialog(private val project: Project?) : DialogWrapper(project, true) {

    private var mainPanel: JPanel? = null
    private var srcDirText: TextFieldWithBrowseButton? = null
    private var pluginConfig: PluginConfig? = null

    init {
        title = "Settings"
        setResizable(true)

        buildViews()
        init()
    }

    private fun buildViews() {
        if (project == null) return

        pluginConfig = PluginConfig.getInstance(project)

        val savedResDir = project.basePath + Constants.DEFAULT_SOURCE_PATH

        srcDirText?.text = savedResDir

        val fileChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
        val selectDir = VirtualFileManager.getInstance().findFileByUrl(savedResDir)
        selectDir?.let {
            fileChooserDescriptor.setRoots(selectDir)
        }
        srcDirText?.addBrowseFolderListener(TextBrowseFolderListener(fileChooserDescriptor, project))
    }

    override fun createCenterPanel() = mainPanel

    override fun doValidate(): ValidationInfo? {
        val srcDir = srcDirText?.text
        if (srcDir.isNullOrEmpty()) {
            return ValidationInfo("Select source directory.")
        }

        return null
    }

    override fun doOKAction() {
        super.doOKAction()

        pluginConfig?.let {
            it.srcDir = srcDirText?.text
        }

        resetContent()
    }

    private fun resetContent() {
        project?.let {
            val drawableViewer = DrawableViewer(it)
            ToolWindowManager.getInstance(it).getToolWindow(Constants.TOOL_WINDOW_ID)?.contentManager?.apply {
                val content = factory.createContent(drawableViewer, null, false)
                removeAllContents(true)
                addContent(content)
            }
        }
    }
}