package com.hamoid.openrndrThumbnails.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Wrench tool icon on top of the window to open SettingsDialog.
 */
class ReloadAction(val callback: () -> Unit) : AnAction(
    // tooltip shown on hover
    "Reload",
    // info shown at window bottom
    "Search for new or changed kotlin files",
    // icon
    AllIcons.Actions.Refresh
) {

    /**
     * Clicking the reload icon executes the callback
     */
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        if (anActionEvent.project != null) {
            callback()
        }
    }
}