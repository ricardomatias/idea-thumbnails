package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.Constants
import com.hamoid.openrndrThumbnails.MyBundle
import com.hamoid.openrndrThumbnails.TransferableImage
import com.hamoid.openrndrThumbnails.action.OpenSettingsAction
import com.hamoid.openrndrThumbnails.action.ReloadAction
import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.IconUtils
import com.hamoid.openrndrThumbnails.utils.IconUtils.Companion.save
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.DataFlavor.imageFlavor
import java.awt.dnd.*
import java.awt.event.*
import java.awt.event.MouseEvent.BUTTON3
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Main Panel
 */
class ThumbsPanel(private val project: Project) :
    SimpleToolWindowPanel(true, true),
    ActionListener {

    private val thumbsRootPath = File("${project.basePath}/.thumbnails")
    private val kotlinFileList = mutableListOf<KotlinFile>()
    private var panelList = JBList(emptyList<JPanel>())
    private var clickedItemId = -1

    init {
        KotlinFile.root = File(project.basePath + Constants.DEFAULT_SOURCE_PATH)
        KotlinFile.rootThumbs = thumbsRootPath

        thumbsRootPath.mkdirs()
        toolbar = createToolbar()
        setContent(createScrollPane())
    }

    /**
     * Create main tool bar with the settings wrench icon
     */
    private fun createToolbar(): JComponent {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(OpenSettingsAction())
        actionGroup.add(ReloadAction(this::reload))
        return ActionManager.getInstance()
            .createActionToolbar(MyBundle.message("name"), actionGroup, true)
            .component
    }

    private fun reload() {
        // TODO: rebuild panel (empty it, populate it)
        println("reload")
    }

    /**
     * Create main content: a scrollable panel with entries
     */
    private fun createScrollPane(): JScrollPane {
        //val pluginConfig = PluginConfig.getInstance(project)

        val files = scanFiles(KotlinFile.root)
        kotlinFileList.addAll(files.map { KotlinFile(it) })

        val panelList = filesToJPanels(kotlinFileList)
        return createScrollPane(panelList)
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
    private fun filesToJPanels(kotlinFiles: List<KotlinFile>) =
        kotlinFiles.map { kotlinFile ->
            val panel = JPanel().apply {
                layout = FlowLayout().apply {
                    alignment = FlowLayout.LEFT
                }
                border = EmptyBorder(2, 4, 2, 4)
            }
            val text = kotlinFile.relativePath()
                .split("/")
                .joinToString("<br>", "<html>", "</html>")

            panel.add(JLabel(text, JLabel.LEFT).apply {
                icon = IconUtils.createSmallIcon(kotlinFile.thumbPath())
                font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
                iconTextGap = 10
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
            cellRenderer = CellStyle()

            addMouseListener(object : MouseListener {
                override fun mouseReleased(e: MouseEvent?) {}
                override fun mouseEntered(e: MouseEvent?) {}
                override fun mouseExited(e: MouseEvent?) {}
                override fun mousePressed(e: MouseEvent?) {}
                override fun mouseClicked(e: MouseEvent?) {
                    if (e == null || panelList.itemsCount == 0) {
                        return
                    }
                    clickedItemId = panelList.locationToIndex(e.point)
                    if (clickedItemId < 0) {
                        return
                    }

                    if (e.button == BUTTON3) {
                        showPopupMenu(e)
                        return
                    }

                    // there's no way to distinguish icon-click vs text-click
                    // https://stackoverflow.com/q/13777411
                    // so I use the local `x` position to distinguish both cases
                    val panel = panelList.model.getElementAt(clickedItemId)
                    val label = panel.getComponent(0) as JLabel
                    if (e.x > label.icon.iconWidth + label.iconTextGap) {
                        edit(clickedItemId)
                    } else {
                        showOriginalImage(clickedItemId)
                    }

                }
            })

            addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent?) {}
                override fun keyReleased(e: KeyEvent?) {}
                override fun keyPressed(e: KeyEvent?) {
                    if (e == null) return
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        showOriginalImage(panelList.minSelectionIndex)
                    }
                    if (e.isControlDown) {
                        when (e.keyChar) {
                            'c' -> println("copy")
                            'v' -> println("paste")
                        }
                    }
                }
            })

            addListSelectionListener {
                clickedItemId = it.firstIndex
                println(clickedItemId)
            }

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
                    if (ev == null) return
                    ev.acceptDrop(DnDConstants.ACTION_COPY)

                    val files = ev.transferable?.getTransferData(
                        DataFlavor.javaFileListFlavor
                    ) as List<*>? ?: listOf("")

                    val img = files.first()
                    if (img is File && img.name.endsWith(Constants.PNG_SUFFIX)) {
                        val i = panelList.locationToIndex(ev.location)
                        if (i >= 0) {
                            val icon =
                                IconUtils.createIcon(img.absolutePath, 640)
                            setLabelIcon(i, icon)
                        }
                    }
                }
            })
        }
        return ScrollPaneFactory.createScrollPane(panelList)
    }

    private fun setLabelIcon(i: Int, icon: ImageIcon) {
        val thumbPath = kotlinFileList[i].thumbPath()
        val panel = panelList.model.getElementAt(i)
        val label = panel.getComponent(0) as JLabel
        icon.save(thumbPath)
        label.icon = IconUtils.createSmallIcon(thumbPath)
        label.setSize(label.width, label.icon.iconHeight)
        label.revalidate()
    }

    /**
     * Launch editor for [KotlinFile] with index [fileIndex]
     */
    private fun edit(fileIndex: Int) {
        LocalFileSystem.getInstance().findFileByIoFile(
            kotlinFileList[fileIndex].file
        )?.let {
            FileEditorManager.getInstance(project)
                .openFile(it, true)
        }
    }

    /**
     * Action performed in popup menu
     */
    override fun actionPerformed(e: ActionEvent?) {
        when (e?.actionCommand) {
            MenuItem.COPY.label -> copyThumbnail()
            MenuItem.PASTE.label -> pasteThumbnail()
        }
    }

    /**
     * Construct and show popup menu
     */
    private fun showPopupMenu(event: MouseEvent?) {
        val popupMenu = JPopupMenu()
        MenuItem.values().forEach {
            popupMenu.add(JMenuItem(it.label).apply {
                addActionListener(this@ThumbsPanel)
            })
        }

        event?.let {
            popupMenu.show(it.component, it.x, it.y)
        }
    }

    /**
     * Show enlarged image
     */
    private fun showOriginalImage(it: Int) =
        OriginalImageDialog(project, kotlinFileList[it]).show()


    /**
     * Copy image thumbnail to clipboard
     */
    private fun copyThumbnail() {
        if (clickedItemId < 0) {
            return
        }
        val thumbPath = kotlinFileList[clickedItemId].thumbPath()
        val icon = IconUtils.createIcon(thumbPath)
        val img = TransferableImage(icon.image)
        CopyPasteManager.getInstance().setContents(img)
    }

    /**
     * Paste clipboard image to thumbnail
     */
    private fun pasteThumbnail() {
        if (clickedItemId < 0) {
            return
        }
        val content = CopyPasteManager.getInstance().contents
        if (content != null && content.isDataFlavorSupported(imageFlavor)) {
            val img = content.getTransferData(imageFlavor) as Image
            setLabelIcon(clickedItemId, ImageIcon(img))
        }
    }
}

/**
 * Available popup menu items
 */
enum class MenuItem(val label: String) {
    COPY("Copy Thumbnail"),
    PASTE("Paste Thumbnail")
}