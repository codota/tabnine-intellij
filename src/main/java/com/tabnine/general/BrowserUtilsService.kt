package com.tabnine.general

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.tabnine.binary.exceptions.NotSupportedByIDEVersion
import com.tabnine.config.Config
import java.lang.reflect.Method
import javax.swing.SwingConstants
interface BrowserUtilsInterface {
    fun openPageOnFocusedProject(pageTitle: String, pageUrl: String) {
    }
    fun openUrlOnSplitWindow(project: Project, pageTitle: String, url: String) {
    }
    fun openUrlOnBrowser(url: String) {
    }
}

class BrowserUtilsServiceDummy : BrowserUtilsInterface {
    companion object {
        @JvmStatic
        val instance = BrowserUtilsServiceDummy()
    }
}
class BrowserUtilsService : BrowserUtilsInterface {
    companion object {
        @JvmStatic
        val instance: BrowserUtilsInterface
            get() = if (Config.IS_ON_PREM) BrowserUtilsServiceDummy.instance else ServiceManager.getService(BrowserUtilsService::class.java)
    }

    override fun openPageOnFocusedProject(pageTitle: String, pageUrl: String) {
        val focusedProject = getFocusedProject()
        if (focusedProject.isPresent) {
            openUrlOnSplitWindow(focusedProject.get(), pageTitle, pageUrl)
        } else {
            openUrlOnBrowser(pageUrl)
        }
    }

    override fun openUrlOnSplitWindow(project: Project, pageTitle: String, url: String) {
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

    override fun openUrlOnBrowser(url: String) {
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
