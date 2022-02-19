package com.androhi.androiddrawableviewer.form

import com.androhi.androiddrawableviewer.Constants
import com.androhi.androiddrawableviewer.PluginConfig
import com.androhi.androiddrawableviewer.action.EditTargetResDirAction
import com.androhi.androiddrawableviewer.model.DrawableModel
import com.androhi.androiddrawableviewer.utils.IconUtils
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import com.intellij.util.ui.TextTransferable
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.*
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder

class DrawableViewer(private val project: Project) :
    SimpleToolWindowPanel(true, true), ActionListener {

    private val drawableModelList = mutableListOf<DrawableModel>()
    private var items = JBList(emptyList<JPanel>())

    private var previousSelectedIndex = 0

    init {
        toolbar = createToolbarPanel()
        setContent(createContentPanel())
    }

    private fun createToolbarPanel(): JComponent {
        val actionGroup = DefaultActionGroup().apply {
            add(EditTargetResDirAction())
        }
        return ActionManager.getInstance()
            .createActionToolbar("Android Drawable Viewer", actionGroup, true)
            .component
    }

    private fun createContentPanel(): JScrollPane {
        createDrawableModelList()
        return createScrollPane(createPanels())
    }

    private fun createDrawableModelList() {
        val pluginConfig = PluginConfig.getInstance(project)
        val srcDir = pluginConfig.srcDir
            ?: (project.basePath + Constants.DEFAULT_SOURCE_PATH)
        val imageFileList = getNewFileList(srcDir, pluginConfig)
        val fileNameList =
            imageFileList.map { it.name }.filter { isImageFile(it) }.distinct()
        fileNameList.forEach { fileName ->
            val model = DrawableModel.create(fileName, imageFileList)
            drawableModelList.add(model)
        }
        drawableModelList.sortBy { it.fileName }
    }

    private fun isImageFile(fileName: String) =
        fileName.endsWith(Constants.PNG_SUFFIX) ||
                fileName.endsWith(Constants.JPEG_SUFFIX)

    private fun getNewFileList(path: String, config: PluginConfig): List<File> {
        val targetDir = File(path)
        if (!targetDir.exists()) {
            return listOf()
        }
        val files = targetDir.listFiles()
        return files?.toList() ?: listOf()
    }

    private fun createPanels(): Vector<JPanel> {
        val panels = Vector<JPanel>(drawableModelList.size)
        drawableModelList.forEach { model ->
            val panel = JPanel().apply {
                layout = FlowLayout()
                border = EmptyBorder(10, 10, 10, 10)
            }

            IconUtils.createSmallIcon(model.getLowDensityFilePath())?.let {
                val iconLabel = JLabel().apply {
                    icon = it
                    text = model.fileName
                    horizontalAlignment = JLabel.LEFT
                    font = Font(Font.SANS_SERIF, Font.PLAIN, 14)
                    iconTextGap = 12
                    addMouseListener(object : MouseListener {
                        override fun mouseClicked(e: MouseEvent?) {
                            println("clicked ${model.fileName}")
                        }
                        override fun mousePressed(e: MouseEvent?) {}
                        override fun mouseReleased(e: MouseEvent?) {}
                        override fun mouseEntered(e: MouseEvent?) {}
                        override fun mouseExited(e: MouseEvent?) {}
                    })
                }
                panel.add(iconLabel)
                panels.add(panel)
            }
        }

        return panels
    }

    private fun createScrollPane(panels: Vector<JPanel>): JScrollPane {
        items = JBList(panels.toMutableList()).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            layoutOrientation = JList.VERTICAL
            cellRenderer = ImageListCellRenderer()

            addMouseListener(object : MouseListener {
                override fun mouseReleased(e: MouseEvent?) {}

                override fun mouseEntered(e: MouseEvent?) {}

                override fun mouseClicked(e: MouseEvent?) {
                    if (items.itemsCount == 0) {
                        return
                    }

                    val selectedIndex = items.selectedIndex
                    if (previousSelectedIndex == selectedIndex) {
                        showPopupMenu(e)
                    }
                    previousSelectedIndex = selectedIndex
                }

                override fun mouseExited(e: MouseEvent?) {}

                override fun mousePressed(e: MouseEvent?) {}
            })
            addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent?) {}

                override fun keyPressed(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) {
                        showDetailDialog()
                    }
                }

                override fun keyReleased(e: KeyEvent?) {}
            })
        }
        return ScrollPaneFactory.createScrollPane(items)
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.actionCommand) {
            MENU_ITEM_SHOW -> showDetailDialog()
            MENU_ITEM_COPY_DRAWABLE_RES -> copyDrawableId()
        }
    }

    private fun showPopupMenu(event: MouseEvent?) {
        val showMenu = JMenuItem(MENU_ITEM_SHOW).apply {
            addActionListener(this@DrawableViewer)
        }
        val copyDrawableIdMenu = JMenuItem(MENU_ITEM_COPY_DRAWABLE_RES).apply {
            addActionListener(this@DrawableViewer)
        }
        val popupMenu = JPopupMenu().apply {
            add(showMenu)
            add(copyDrawableIdMenu)
        }

        event?.let {
            popupMenu.show(it.component, it.x, it.y)
        }
    }

    // popup action 1
    private fun showDetailDialog() {
        items.minSelectionIndex.let { index: Int ->
            DetailDisplayDialog(project, drawableModelList[index]).show()
        }
    }

    // popup action 2
    private fun copyDrawableId() {
        items.minSelectionIndex.let { index: Int ->
            var fileName = drawableModelList[index].fileName
            val periodPosition = fileName.lastIndexOf(".")
            if (periodPosition >= 0) {
                fileName = fileName.substring(0, periodPosition)
            }
            val fileNameWithoutExtension = fileName
            val str =
                java.lang.StringBuilder("R.drawable.$fileNameWithoutExtension")
            CopyPasteManager.getInstance().setContents(TextTransferable(str))
        }
    }

    companion object {
        const val MENU_ITEM_SHOW = "Show"
        const val MENU_ITEM_COPY_DRAWABLE_RES = "Copy Drawable Res"
    }
}