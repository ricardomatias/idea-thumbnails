package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.Constants
import com.hamoid.openrndrThumbnails.action.OpenSettingsAction
import com.hamoid.openrndrThumbnails.action.ReloadAction
import com.hamoid.openrndrThumbnails.model.KotlinFile
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
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Main Panel
 */
class ThumbsPanel(private val project: Project) :
    SimpleToolWindowPanel(true, true), ActionListener {

    private val kotlinFileList = mutableListOf<KotlinFile>()
    private var panelList = JBList(emptyList<JPanel>())
    private var previousSelectedIndex = 0

    init {
        toolbar = createToolbarPanel()
        setContent(createContentPanel())
    }

    /**
     * Create main tool bar with the settings wrench icon
     */
    private fun createToolbarPanel(): JComponent {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(OpenSettingsAction())
        actionGroup.add(ReloadAction(this::reload))
        return ActionManager.getInstance()
            .createActionToolbar("OPENRNDR Thumbnails", actionGroup, true)
            .component
    }

    private fun reload() {
        // TODO: rebuild panel (empty it, populate it)
        println("reload")
    }

    /**
     * Create main content: a scrollable panel with entries
     */
    private fun createContentPanel(): JScrollPane {
        //val pluginConfig = PluginConfig.getInstance(project)
        KotlinFile.root = File(project.basePath + Constants.DEFAULT_SOURCE_PATH)
        val files = scanFiles(KotlinFile.root)
        kotlinFileList.addAll(files.map { KotlinFile(it) })
        val panels = filesToPanels(kotlinFileList)
        return createScrollPane(panels)
    }

    /**
     * Scans folder for Kotlin files
     */
    private fun scanFiles(targetDir: File): List<File> {
        if (!targetDir.exists()) {
            return listOf()
        }
        return targetDir.walkTopDown().toList().filter {
            it.name.endsWith(Constants.KT_SUFFIX)
        }.sorted()
    }

    /**
     * Converts list of [KotlinFile] to UI panels
     */
    private fun filesToPanels(kotlinFiles: List<KotlinFile>) =
        kotlinFiles.map { model ->
            val panel = JPanel().apply {
                layout = FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                }
                border = EmptyBorder(8, 8, 8, 8)
            }

            panel.add(JLabel(model.relativePath(), JLabel.LEFT).apply {
                //icon = IconUtils.createSmallIcon(model.fileName)
                font = Font(Font.SANS_SERIF, Font.PLAIN, 12)
                iconTextGap = 12
                // TODO: distinguish clicking on image and on text
                addMouseListener(object : MouseListener {
                    override fun mousePressed(e: MouseEvent?) {}
                    override fun mouseReleased(e: MouseEvent?) {}
                    override fun mouseEntered(e: MouseEvent?) {}
                    override fun mouseExited(e: MouseEvent?) {}
                    override fun mouseClicked(e: MouseEvent?) {
                        println("clicked ${model.path}")
                    }
                })
            })
            panel
        }


    /**
     * Creates a scroll pane with all the panels, add event listeners
     */
    private fun createScrollPane(items: List<JPanel>): JScrollPane {
        panelList = JBList(items)
        //panelList.removeAll()
        panelList.apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            layoutOrientation = JList.VERTICAL
            cellRenderer = ImageListCellRenderer()

            addMouseListener(object : MouseListener {
                override fun mouseReleased(e: MouseEvent?) {}
                override fun mouseEntered(e: MouseEvent?) {}
                override fun mouseExited(e: MouseEvent?) {}
                override fun mousePressed(e: MouseEvent?) {}
                override fun mouseClicked(e: MouseEvent?) {
                    if (panelList.itemsCount > 0) {
                        if (previousSelectedIndex == panelList.selectedIndex) {
                            showPopupMenu(e)
                        } else {
                            previousSelectedIndex = panelList.selectedIndex
                        }
                    }
                }
            })

            addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent?) {}
                override fun keyReleased(e: KeyEvent?) {}
                override fun keyPressed(e: KeyEvent?) {
                    if (e?.keyCode == KeyEvent.VK_ENTER) {
                        showOriginalImage()
                    }
                }
            })

            /**
             * Use to drop images into the list, then assign that image
             * as a thumbnail for the target .kt program
             */
            dropTarget = DropTarget(this, object : DropTargetListener {
                override fun dragEnter(dtde: DropTargetDragEvent?) {}
                override fun dragOver(dtde: DropTargetDragEvent?) {}
                override fun dropActionChanged(dtde: DropTargetDragEvent?) {}
                override fun dragExit(dte: DropTargetEvent?) {}
                override fun drop(ev: DropTargetDropEvent?) {
                    ev?.acceptDrop(DnDConstants.ACTION_COPY)

                    val files = ev?.transferable?.getTransferData(
                        DataFlavor.javaFileListFlavor
                    ) as List<*>? ?: listOf("")

                    files.forEach {
                        if (it is File) {
                            println("dropped file: ${it.absolutePath}")
                        }
                    }
                }
            })
        }
        return ScrollPaneFactory.createScrollPane(panelList)
    }

    /**
     * Action performed in popup menu
     */
    override fun actionPerformed(e: ActionEvent?) {
        when (e?.actionCommand) {
            MenuItem.SHOW.label -> showOriginalImage()
            MenuItem.COPY_SOMETHING.label -> copyDrawableId()
        }
    }

    /**
     * Construct and show popup menu
     */
    private fun showPopupMenu(event: MouseEvent?) {
        val popupMenu = JPopupMenu().apply {
            add(JMenuItem(MenuItem.SHOW.label).apply {
                addActionListener(this@ThumbsPanel)
            })
            add(JMenuItem(MenuItem.COPY_SOMETHING.label).apply {
                addActionListener(this@ThumbsPanel)
            })
        }

        event?.let {
            popupMenu.show(it.component, it.x, it.y)
        }
    }

    /**
     * Popup action 1
     */
    private fun showOriginalImage() {
        panelList.minSelectionIndex.let { index: Int ->
            OriginalImageDialog(project, kotlinFileList[index]).show()
        }
    }

    /**
     * Popup action 2
     */
    private fun copyDrawableId() {
        panelList.minSelectionIndex.let { //index: Int ->
//            var fileName = drawableModelList[index].fileName
//            val periodPosition = fileName.lastIndexOf(".")
//            if (periodPosition >= 0) {
//                fileName = fileName.substring(0, periodPosition)
//            }
//            val fileNameWithoutExtension = fileName
            val str = java.lang.StringBuilder("foo")
            CopyPasteManager.getInstance().setContents(TextTransferable(str))
        }
    }
}

/**
 * Available popup menu items
 */
enum class MenuItem(val label: String) {
    SHOW("Show"),
    COPY_SOMETHING("Copy Something")
}