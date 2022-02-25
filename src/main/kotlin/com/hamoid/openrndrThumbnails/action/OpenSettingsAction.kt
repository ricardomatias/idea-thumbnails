package com.hamoid.openrndrThumbnails.action

import com.hamoid.openrndrThumbnails.form.SettingsDialog
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Wrench tool icon on top of the window to open SettingsDialog.
 */
class OpenSettingsAction : AnAction(
    "Settings", // tooltip shown on hover
    "Configure the OPENRNDR thumbs plugin", // info shown at window bottom
    AllIcons.General.Settings // wrench icon
) {

    /**
     * Clicking the wrench icon opens the settings dialog
     */
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        project?.let {
            SettingsDialog(it).show()
        }
    }
}