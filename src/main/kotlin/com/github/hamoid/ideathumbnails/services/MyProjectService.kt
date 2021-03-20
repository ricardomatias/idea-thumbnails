package com.github.hamoid.ideathumbnails.services

import com.github.hamoid.ideathumbnails.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
