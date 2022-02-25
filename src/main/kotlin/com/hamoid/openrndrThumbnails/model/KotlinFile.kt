package com.hamoid.openrndrThumbnails.model

import java.io.File
import java.util.*

/**
 * A model representing a Kotlin file.
 */
data class KotlinFile(val path: File) {
    fun relativePath(): String = path.relativeTo(root).path

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

        val lines = path.readLines().toMutableList()
        val i = 1 + lines.indexOfLast { it.startsWith("import ") }
        lines.add(i, header)

        val newPath = File("/tmp/src/${relativePath()}")
        newPath.parentFile.mkdirs()
        newPath.writeText(lines.joinToString("\n"))
    }

    /**
     * Parses header ([id], [description], [tags]) from [content]
     */
    private fun parseHeader() {
        val blockComment = rxBlockComment.find(content)
        if (blockComment != null) {
            val commentLines = blockComment.groupValues[1].split("\n")
            val compactHeader = commentLines
                .map { it.trim('*', ' ') }
                .joinToString(" ")
            val props = rxProperties.find(compactHeader)
            if (props != null) {
                id = props.groupValues[1]
                description = props.groupValues[2]
                tags = props.groupValues[3]
            } else {
                id = UUID.randomUUID().toString()
                description = "New sketch"
                tags = "#new"
                saveWithNewHeader()
            }
        }
    }

    fun reload() {
        content = path.readText()
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