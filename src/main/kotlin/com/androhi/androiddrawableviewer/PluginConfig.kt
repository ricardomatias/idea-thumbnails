package com.androhi.androiddrawableviewer

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
        name = "AndroidDrawableViewer",
        reloadable = true,
        storages = [
            Storage(file = "\$PROJECT_FILE\$"),
            Storage(file = "\$PROJECT_CONFIG_DIR\$/drawable_viewer_plugin.xml")
        ]
)

class PluginConfig : PersistentStateComponent<PluginConfig> {

    var srcDir: String? = null

    override fun getState(): PluginConfig = this

    override fun loadState(pluginConfig: PluginConfig) {
        pluginConfig.let {
            XmlSerializerUtil.copyBean(it, this)
        }
    }

    companion object {
        fun getInstance(project: Project): PluginConfig = ServiceManager.getService(project, PluginConfig::class.java)
    }
}