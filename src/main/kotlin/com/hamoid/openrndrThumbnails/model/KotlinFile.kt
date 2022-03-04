package com.hamoid.openrndrThumbnails.model

import com.hamoid.openrndrThumbnails.MyBundle
import com.hamoid.openrndrThumbnails.form.KotlinFilePanel
import java.io.File
import java.util.*

/**
 * Represents a Kotlin file.
 * Includes metadata ([id], [description], [tags]),
 * the [fileContent] of the file and a Swing [panel] component to display
 * in a scrollable list.
 */
data class KotlinFile(val file: File) {
    val relativePath: String
        get() = file.relativeTo(root).path

    val thumbPath: String
        get() = "$rootThumbs/$id.png"

    private lateinit var fileContent: String

    // metadata from the file
    lateinit var id: String
    lateinit var description: String
    lateinit var tags: String

    // A string combining metadata and tokens to be searched on
    lateinit var searchable: String

    /**
     * Contains the UI component to display in a list.
     */
    val panel = KotlinFilePanel(this)

    init {
        reload()
    }

    /**
     * Reads the Kotlin [file] content from disk,
     * extracting [id], [description] and [tags] from the header.
     * If the header is not found it generates a random [id] and s
     * aves the file to disk.
     */
    fun reload() {
        fileContent = file.readText()

        // find first block comment in the kt file
        val blockComment = rxBlockComment.find(fileContent)
        if (blockComment != null) {
            // trim the comment lines removing ' ' and '*' and
            // join into one long line separated with spaces
            val commentLines = blockComment.groupValues[1].split("\n")
            val compactHeader = commentLines.joinToString(" ") {
                it.trim('*', ' ')
            }
            // in that line, try find metadata
            val props = rxMetadata.find(compactHeader)
            if (props != null) {
                // if found, set metadata and return
                updateMetadata(props.groupValues.drop(1))
                return
            }
        }
        // if not found, generate metadata and save .kt file
        updateMetadata(
            listOf(UUID.randomUUID().toString(), "New sketch", "#new")
        )
        saveWithNewHeader()
    }

    /**
     * Update [id], [description], [tags], [searchable] and then rebuild the [panel]
     */
    private fun updateMetadata(metadata: List<String>) {
        id = metadata[0]
        description = metadata[1]
        tags = metadata[2]

        val tokens = fileContent.toLowerCase().split(Regex("\\W|\\d")).filter {
            it.length > 2 && it !in ignoredTokens
        }.distinct().joinToString(" ")

        searchable = "${relativePath.toLowerCase()} $tags $tokens $description"

        panel.rebuild()
    }

    /**
     * Saves the Kotlin file to disk with the injected header.
     */
    private fun saveWithNewHeader() {
        val header = """
            
        /**
         * id: $id
         * description: $description
         * tags: $tags
         */    
        """.trimIndent()

        val lines = file.readLines().toMutableList()
        val i = 1 + lines.indexOfLast { it.startsWith("import ") }
        lines.add(i, header)

        file.writeText(
            lines.joinToString("\n", postfix = "\n")
        )
    }

    companion object {
        // Set before creating any KotlinFile instances!
        lateinit var rootThumbs: File
        lateinit var root: File

        // rx to get inner content of /** ??? */
        val rxBlockComment = Regex(
            """/\*\*(.*?)\*/""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )

        // rx to extract metadata from String.
        val rxMetadata = Regex(
            """id:\h*(\S+)\h+description:\h*(.*?)\h+tags:\h*(\V*?)$"""
        )

        // Tokens that are too frequent in my OPENRNDR programs to care about.
        // In other words, searching for these would return all programs.
        val ignoredTokens = MyBundle.message("ignoreTokens")
            .split(" ").filterNot { it.isEmpty() }
    }
}