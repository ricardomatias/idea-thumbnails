package com.hamoid.openrndrThumbnails

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
        name = "OPENRDRThumbnails",
        reloadable = true,
        storages = [
            Storage("\$PROJECT_FILE\$"),
            Storage("\$PROJECT_CONFIG_DIR\$/openrndr_thumbnails_plugin.xml")
        ]
)

class PluginConfig : PersistentStateComponent<PluginConfig> {

    var srcDir: String? = null

    override fun getState() = this

    override fun loadState(pluginConfig: PluginConfig) {
        XmlSerializerUtil.copyBean(pluginConfig, this)
    }

    companion object {
        fun getInstance(project: Project): PluginConfig =
            ServiceManager.getService(project, PluginConfig::class.java)
    }
}