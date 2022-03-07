package com.hamoid.openrndrThumbnails.form

import com.hamoid.openrndrThumbnails.Constants
import com.hamoid.openrndrThumbnails.MyBundle
import com.hamoid.openrndrThumbnails.TransferableImage
import com.hamoid.openrndrThumbnails.action.OpenSettingsAction
import com.hamoid.openrndrThumbnails.action.ReloadAction
import com.hamoid.openrndrThumbnails.model.KotlinFile
import com.hamoid.openrndrThumbnails.utils.DelayedChangeListener
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
    private var selectedKotlinFile: KotlinFile? = null
    private var panelList = JBList(emptyList<JLabel>())
    private var lastClickMs = -1L

    private val debug = JTextArea(4, 20)

    init {
        KotlinFile.root = File(project.basePath + Constants.DEFAULT_SOURCE_PATH)
        KotlinFile.rootThumbs = thumbsRootPath

        // log("hello")
        thumbsRootPath.mkdirs()
        toolbar = createToolbar()
        setContent(createContent())
    }

    // private fun log(msg: String) = debug.append("$msg\n")

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

    /**
     * Reload files from disk, parsing metadata
     */
    private fun reload() {
        allKotlinFiles.forEach { it.setup() }
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
            if (it.name.endsWith(Constants.KT_SUFFIX)) {
                val t = it.readText()
                return@filter t.contains("openrndr") &&
                        t.contains("application")
            } else {
                return@filter false
            }
        }.sorted().forEach {
            allKotlinFiles.add(KotlinFile(it))
        }
        filteredKotlinFiles.clear()
        filteredKotlinFiles.addAll(allKotlinFiles)
    }


    /**
     * Creates a filtered copy of [allKotlinFiles].
     * Could also be used for sorting.
     */
    private fun filter(str: String) {
        val keyword = str.toLowerCase()
        filteredKotlinFiles.clear()
        filteredKotlinFiles.addAll(
            allKotlinFiles.filter {
                it.searchable.contains(keyword)
            }
        )

        val model = panelList.model as DefaultListModel
        model.removeAllElements()
        System.gc()
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
                    val i = panelList.locationToIndex(e.point)
                    if (i < 0) {
                        return
                    }

                    selectedKotlinFile = filteredKotlinFiles[i]

                    if (e.button == BUTTON3) {
                        showPopupMenu(e)
                        return
                    }

                    val textClicked =
                        selectedKotlinFile!!.panel.isTextClick(e.x)
                    val currentClickMs = System.currentTimeMillis()
                    val doubleClick = currentClickMs - lastClickMs < 400

                    if (doubleClick) {
                        if (textClicked || selectedKotlinFile!!.panel.hasNoThumb()) {
                            editCode()
                        } else {
                            showOriginalImage()
                        }
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
                        showOriginalImage()
                    }
                    if (e.keyCode == KeyEvent.VK_DELETE) {
                        selectedKotlinFile?.panel?.clearThumb()
                        refresh()
                    }
                }
            })

            addListSelectionListener {
                selectedKotlinFile = filteredKotlinFiles[
                        panelList.minSelectionIndex.coerceAtLeast(0)
                ]
            }

            // Used to drop images into the list, then assign that image
            // as a thumbnail for the target .kt program
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

                    val f = droppedFiles.first()
                    if (f is File && (f.name.endsWith(Constants.PNG_SUFFIX) ||
                                f.name.endsWith(Constants.JPEG_SUFFIX))
                    ) {
                        val i = panelList.locationToIndex(ev.location)
                        if (i >= 0) {
                            filteredKotlinFiles[i].panel.setThumb(
                                f.absolutePath
                            )
                            refresh()
                        }
                    }
                }
            })
        }
        return ScrollPaneFactory.createScrollPane(panelList)
    }

    /**
     * Hack to redraw the list. Otherwise, icon size changes
     * do not resize the surrounding label.
     */
    private fun refresh() {
        (panelList.model as DefaultListModel).let { model ->
            model.addElement(JLabel())
            model.remove(model.size() - 1)
        }
    }

    /**
     * Launch editor for [KotlinFile] with index [it]
     */
    private fun editCode() {
        LocalFileSystem.getInstance().findFileByIoFile(
            selectedKotlinFile!!.file
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
    private fun showOriginalImage() =
        OriginalImageDialog(project, selectedKotlinFile!!).show()

    /**
     * Copy image thumbnail to clipboard
     */
    private fun copyThumbnail() {
        if (selectedKotlinFile != null) {
            CopyPasteManager.getInstance().setContents(
                TransferableImage(
                    selectedKotlinFile!!.panel.getThumbLarge().image
                )
            )
        }
    }

    /**
     * Paste clipboard image to thumbnail
     */
    private fun pasteThumbnail() {
        val content = CopyPasteManager.getInstance().contents
        if (content != null && content.isDataFlavorSupported(imageFlavor)) {
            selectedKotlinFile?.panel?.setThumb(
                content.getTransferData(imageFlavor) as Image
            )
            refresh()
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