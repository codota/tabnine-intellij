package com.tabnine.general

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.tabnine.binary.exceptions.NotSupportedByIDEVersion
import java.lang.reflect.Method
import javax.swing.SwingConstants

class BrowserUtilsService {
    companion object {
        @JvmStatic
        val instance: BrowserUtilsService
            get() = ServiceManager.getService(BrowserUtilsService::class.java)
    }

    fun openPageOnFocusedProject(pageTitle: String, pageUrl: String) {
        val focusedProject = getFocusedProject()
        if (focusedProject.isPresent) {
            openUrlOnSplitWindow(focusedProject.get(), pageTitle, pageUrl)
        } else {
            openUrlOnBrowser(pageUrl)
        }
    }

    fun openUrlOnSplitWindow(project: Project, pageTitle: String, url: String) {
        try {
            val openEditorMethod = getOpenEditorMethod()
            val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project)
            // this causes the current focused file to be displayed twice - in the old and in the new editor
            fileEditorManagerEx.createSplitter(
                SwingConstants.VERTICAL, fileEditorManagerEx.currentWindow
            )
            // save the current focused file, as we want to close it after the web page is loaded
            val currentFocusedFile = fileEditorManagerEx.currentWindow?.selectedFile
            // the focus moves to the web page
            openEditorMethod.invoke(
                null,
                project,
                pageTitle,
                url,
                null
            )
            // close the duplicated file in the new editor and set the web page to be the single tab
            if (currentFocusedFile != null) {
                fileEditorManagerEx.currentWindow?.closeFile(currentFocusedFile)
            }
        } catch (e: NotSupportedByIDEVersion) {
            openUrlOnBrowser(url)
        }
    }

    fun openUrlOnBrowser(url: String) {
        BrowserUtil.browse(url)
    }

    @Throws(NotSupportedByIDEVersion::class)
    private fun getOpenEditorMethod(): Method {
        return try {
            HTMLEditorProvider::class.java.getMethod(
                "openEditor",
                Project::class.java,
                String::class.java,
                String::class.java,
                String::class.java
            )
        } catch (e: Exception) {
            throw NotSupportedByIDEVersion()
        }
    }
}
