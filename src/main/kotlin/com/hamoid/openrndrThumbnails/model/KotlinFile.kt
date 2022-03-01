package com.hamoid.openrndrThumbnails.model

import java.io.File
import java.util.*

/**
 * A model representing a Kotlin file.
 */
data class KotlinFile(val file: File) {
    fun relativePath(): String = file.relativeTo(root).path
    fun thumbPath(): String = "$rootThumbs/$id.png"

    private lateinit var content: String

    lateinit var id: String
    lateinit var description: String
    lateinit var tags: String

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

    /**
     * Parses header ([id], [description], [tags]) from [content]
     */
    private fun parseHeader() {
        // find first block comment in the kt file
        val blockComment = rxBlockComment.find(content)
        if (blockComment != null) {
            // trim the comment lines removing ' ' and '*' and
            // join into one long line separated with spaces
            val commentLines = blockComment.groupValues[1].split("\n")
            val compactHeader = commentLines.joinToString(" ") {
                it.trim('*', ' ')
            }
            // in that line, try find id,  description and tags
            val props = rxProperties.find(compactHeader)
            if (props != null) {
                // if found, set vars and return
                id = props.groupValues[1]
                description = props.groupValues[2]
                tags = props.groupValues[3]
                return
            }
        }
        // if not found, generate id, description and tags, save file
        id = UUID.randomUUID().toString()
        description = "New sketch"
        tags = "#new"
        saveWithNewHeader()
    }

    fun reload() {
        content = file.readText()
        parseHeader()
    }

    init {
        reload()
    }

    @OptIn(ExperimentalStdlibApi::class)
    val tokens: String by lazy {
        content.lowercase()
            .split(Regex("\\W|\\d"))
            .filter {
                it.length > 2 && it !in ignoredTokens
            }
            .distinct()
            .joinToString(" ")
    }

    companion object {
        // Set before creating any KotlinFile instances!
        lateinit var rootThumbs: File
        lateinit var root: File

        // rx to get inner content of /** ??? */
        val rxBlockComment = Regex(
            """\/\*\*(.*?)\*\/""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )

        // rx to extract id, description and tags from String.
        val rxProperties = Regex(
            """id:\h*(\S+)\h+description:\h*(.*?)\h+tags:\h*(\V*?)$"""
        )

        // Tokens that are too frequent in my OPENRNDR programs to care about.
        // In other words, searching for these would return all programs.
        val ignoredTokens = listOf(
            "application", "configure", "extend", "fun", "funpro",
            "height", "import", "main", "openrndr", "org",
            "package", "program", "val", "var", "width",
        )
    }
}