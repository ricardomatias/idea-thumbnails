package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.Constants
import com.hamoid.openrndrThumbnails.MyBundle
import com.hamoid.openrndrThumbnails.TransferableImage
import com.hamoid.openrndrThumbnails.action.OpenSettingsAction
import com.hamoid.openrndrThumbnails.action.ReloadAction
import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.DelayedChangeListener
import com.hamoid.openrndrThumbnails.utils.IconUtils
import com.hamoid.openrndrThumbnails.utils.IconUtils.Companion.save
import com.hamoid.openrndrThumbnails.utils.IconUtils.Companion.scaled
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.FlowLayout
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.DataFlavor.imageFlavor
import java.awt.dnd.*
import java.awt.event.*
import java.awt.event.MouseEvent.BUTTON3
import java.io.File
import javax.swing.*


/**
 * Main Panel
 */
class ThumbsPanel(private val project: Project) :
    SimpleToolWindowPanel(true, true),
    ActionListener {

    private val thumbsRootPath = File("${project.basePath}/.thumbnails")
    private val allKotlinFiles = mutableListOf<KotlinFile>()
    private val filteredKotlinFiles = mutableListOf<KotlinFile>()
    private var panelList = JBList(emptyList<JPanel>())
    private var clickedItemId = -1
    private var lastClickMs = -1L

    private val debug = JTextArea(4, 20)

    init {
        KotlinFile.root = File(project.basePath + Constants.DEFAULT_SOURCE_PATH)
        KotlinFile.rootThumbs = thumbsRootPath

        log("hello")
        thumbsRootPath.mkdirs()
        toolbar = createToolbar()
        setContent(createContent())
    }

    private fun log(msg: String) = debug.append("$msg\n")

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
        log("reload")
    }

    /**
     * Create main content: a scrollable panel with entries
     */
    private fun createContent(): JComponent {
        //val pluginConfig = PluginConfig.getInstance(project)

        val panel = JPanel()
        panel.layout = BorderLayout()

        // 1. Top filter text input
        val filterTextField = JTextField(20)
        filterTextField.document.addDocumentListener(DelayedChangeListener().apply {
            addChangeListener {
                filter(filterTextField.text)
            }
        })
        panel.add(filterTextField, BorderLayout.NORTH)

        // 2. Scrollable thumbnail list
        panel.add(createScrollPane(), BorderLayout.CENTER)

        // 3. Debug text area
        //panel.add(debug, BorderLayout.SOUTH)

        return panel
    }

    /**
     * Scans folder for Kotlin files
     */
    private fun populateKotlinFileList() {
        allKotlinFiles.clear()
        if (!KotlinFile.root.exists()) {
            return
        }
        KotlinFile.root.walkTopDown().toList().filter {
            it.name.endsWith(Constants.KT_SUFFIX)
        }.sorted().forEach {
            allKotlinFiles.add(KotlinFile(it))
        }
        filteredKotlinFiles.clear()
        filteredKotlinFiles.addAll(allKotlinFiles)
    }


    @OptIn(ExperimentalStdlibApi::class)
    private fun filter(str: String) {
        val keyword = str.lowercase()
        filteredKotlinFiles.clear()
        filteredKotlinFiles.addAll(
            allKotlinFiles.filter {
                it.relativePath().lowercase().contains(keyword)
            }
        )

        val model = panelList.model as DefaultListModel
        model.removeAllElements()
        filteredKotlinFiles.forEach {
            model.addElement(it.panel)
        }
        panelList.model = model
    }

    /**
     * Creates a scroll pane with all the panels, add event listeners
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun createScrollPane(): JScrollPane {
        populateKotlinFileList()
        panelList = JBList(filteredKotlinFiles.map { it.panel })
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

                    val panel = panelList.model.getElementAt(clickedItemId)
                    val label = panel.getComponent(0) as JLabel
                    // there's no way to distinguish icon-click vs text-click
                    // https://stackoverflow.com/q/13777411
                    // so I use the `x` position to distinguish between them
                    val textClicked =
                        e.x > label.icon.iconWidth + label.iconTextGap / 2
                    val currentClickMs = System.currentTimeMillis()

                    if (textClicked) {
                        val doubleClick = currentClickMs - lastClickMs < 300
                        if (doubleClick) {
                            edit(clickedItemId)
                        }
                    } else {
                        showOriginalImage(clickedItemId)
                    }
                    lastClickMs = currentClickMs
                }
            })

            registerKeyboardAction(
                { copyThumbnail() }, "Copy",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
                JComponent.WHEN_FOCUSED
            )

            registerKeyboardAction(
                { pasteThumbnail() }, "Paste",
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK),
                JComponent.WHEN_FOCUSED
            )

            addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent?) {}
                override fun keyReleased(e: KeyEvent?) {}
                override fun keyPressed(e: KeyEvent?) {
                    if (e == null) return
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        showOriginalImage(panelList.minSelectionIndex)
                    }
                    if (e.keyCode == KeyEvent.VK_DELETE) {
                        deleteThumb(clickedItemId)
                    }
                }
            })

            addListSelectionListener {
                clickedItemId = panelList.minSelectionIndex
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

                    val droppedFiles = ev.transferable?.getTransferData(
                        DataFlavor.javaFileListFlavor
                    ) as List<*>? ?: listOf("")

                    log("Dropped")
                    val droppedFile = droppedFiles.first()
                    if (droppedFile is File && droppedFile.name.endsWith(
                            Constants.PNG_SUFFIX
                        )
                    ) {
                        log("It's a PNG!")
                        val i = panelList.locationToIndex(ev.location)
                        if (i >= 0) {
                            log("index is $i")
                            val icon =
                                IconUtils.loadImage(droppedFile.absolutePath)
                            setLabelIcon(i, icon)
                        }
                    }
                }
            })
        }
        return ScrollPaneFactory.createScrollPane(panelList)
    }

    private fun setLabelIcon(i: Int, img: Image) {
        val thumbPath = filteredKotlinFiles[i].thumbPath()
        val panel = panelList.model.getElementAt(i)
        val label = panel.getComponent(0) as JLabel

        if (img.getWidth(null) > 640) {
            img.scaled(640).save(thumbPath)
        } else {
            img.save(thumbPath)
        }

        label.icon = IconUtils.createSmallIcon(thumbPath)
        label.text = label.text + ""

        val model = panelList.model as DefaultListModel
        model.addElement(JPanel())
        model.remove(model.size() - 1)
    }

    /**
     * Launch editor for [KotlinFile] with index [fileIndex]
     */
    private fun edit(fileIndex: Int) {
        LocalFileSystem.getInstance().findFileByIoFile(
            filteredKotlinFiles[fileIndex].file
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
        OriginalImageDialog(project, filteredKotlinFiles[it]).show()

    /**
     * Delete thumbnail
     */
    private fun deleteThumb(id: Int) {
        if (clickedItemId >= 0) {
            val f = filteredKotlinFiles[id]
            if (IconUtils.deleteIcon(f.thumbPath())) {
                setLabelIcon(clickedItemId, IconUtils.emptyImage())
            }
        }
    }

    /**
     * Copy image thumbnail to clipboard
     */
    private fun copyThumbnail() {
        if (clickedItemId >= 0) {
            val thumbPath = filteredKotlinFiles[clickedItemId].thumbPath()
            val icon = IconUtils.createIcon(thumbPath)
            val img = TransferableImage(icon.image)
            CopyPasteManager.getInstance().setContents(img)
        }
    }

    /**
     * Paste clipboard image to thumbnail
     */
    private fun pasteThumbnail() {
        if (clickedItemId >= 0) {
            val content = CopyPasteManager.getInstance().contents
            if (content != null && content.isDataFlavorSupported(imageFlavor)) {
                val img = content.getTransferData(imageFlavor) as Image
                setLabelIcon(clickedItemId, img)
            }
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