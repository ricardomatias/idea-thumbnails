package com.androhi.androiddrawableviewer.model

import com.androhi.androiddrawableviewer.Constants.Companion.PATH_SEPARATOR
import java.io.File

/**
 * One model is created per file.
 * I'm not sure if it needs a model.
 *
 */
class DrawableModel private constructor(
    val fileName: String,
    val filePathList: List<String>
) {
    fun getLowDensityFilePath() = filePathList[0]

    companion object {
        fun create(
            fileName: String, allFilePathArray: List<File>?
        ): DrawableModel {
            val filePathList = mutableListOf<String>()

            allFilePathArray?.forEach { file ->
                val filePath = file.path
                val pathArray = filePath.split(PATH_SEPARATOR)
                if (fileName != pathArray[pathArray.size - 1]) {
                    return@forEach
                }
                filePathList.add(filePath)
            }

            return DrawableModel(fileName, filePathList)
        }
    }

    override fun toString() = "model for $fileName"
}