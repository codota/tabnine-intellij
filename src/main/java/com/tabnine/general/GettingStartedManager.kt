package com.tabnine.general

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.tabnine.general.Utils.executeUIThreadWithDelay
import java.lang.reflect.Method
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.swing.SwingConstants

const val PAGE_TITLE = "Tabnine - Getting Started"
const val PAGE_URL = "https://www.tabnine.com/getting-started/intellij?origin=ide"
const val IS_GETTING_STARTED_OPENED_KEY = "is-getting-started-opened"
const val AFTER_PROJECT_OPENED_DELAY_SECONDS = 15L

fun handleFirstTimePreview() {
    if (!isPageShown()) {
        executeUIThreadWithDelay(
            { openPageOnAllProjects() },
            AFTER_PROJECT_OPENED_DELAY_SECONDS,
            TimeUnit.SECONDS
        )
    }
}

fun openPageOnProject(project: Project) {
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
            PAGE_TITLE,
            PAGE_URL,
            null
        )
        // close the duplicated file in the new editor and set the web page to be the single tab
        if (currentFocusedFile != null) {
            fileEditorManagerEx.currentWindow?.closeFile(currentFocusedFile)
        }
    } catch (e: Exception) {
        BrowserUtil.browse(PAGE_URL)
    }
    markPageAsShown()
}

private fun openPageOnAllProjects() {
    if (isInIdeWebPageSupported()) {
        Arrays.stream(ProjectManager.getInstance().openProjects)
            .forEach { openPageOnProject(it) }
    } else {
        BrowserUtil.browse(PAGE_URL)
    }
}

private fun isInIdeWebPageSupported(): Boolean {
    return try {
        getOpenEditorMethod()
        true
    } catch (e: Exception) {
        false
    }
}

private fun getOpenEditorMethod(): Method {
    return HTMLEditorProvider::class.java.getMethod(
        "openEditor",
        Project::class.java,
        String::class.java,
        String::class.java,
        String::class.java
    )
}

private fun isPageShown(): Boolean {
    return PropertiesComponent.getInstance().getBoolean(IS_GETTING_STARTED_OPENED_KEY)
}

private fun markPageAsShown() {
    PropertiesComponent.getInstance().setValue(IS_GETTING_STARTED_OPENED_KEY, true)
}
