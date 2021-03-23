package com.github.hamoid.ideathumbnails.listeners

import com.github.hamoid.ideathumbnails.services.MyProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()
    }

    /*
    // https://plugins.jetbrains.com/docs/intellij/psi-files.html
    To find files with a specific name anywhere in the project, use
    FilenameIndex.getFilesByName(project, name, scope)
     */

    /*
    // This demo seems useful
    // https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/project_view_pane

    describes an implementation of the com.intellij.projectViewPane extension
    point, which allows creating an additional presentation type for the
    Project view pane. ImagesProjectViewPane limits the project tree to
    the images only.
    */
}
