package com.tabnine.general

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.HTMLEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.tabnine.general.Utils.executeEdtWithDelay
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.swing.SwingConstants

const val PAGE_TITLE = "Tabnine - Getting Started"
const val PAGE_URL = "https://www.tabnine.com/getting-started/intellij?origin=ide"
const val IS_GETTING_STARTED_OPENED = "is-getting-started-opened"
const val AFTER_PROJECT_OPENED_DELAY_SECONDS = 15L

object GettingStartedManager {

    @JvmStatic
    fun handleFirstTimePreview() {
        if (!isPageShown()) {
            executeEdtWithDelay(
                { openPageOnAllProjects() },
                AFTER_PROJECT_OPENED_DELAY_SECONDS,
                TimeUnit.SECONDS
            )
        }
    }

    @JvmStatic
    fun openPageOnAllProjects() {
        Arrays.stream(ProjectManager.getInstance().openProjects)
            .forEach { openPageOnProject(it) }
    }

    @JvmStatic
    fun openPageOnProject(project: Project) {
        val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project)
        fileEditorManagerEx.createSplitter(
            SwingConstants.VERTICAL, fileEditorManagerEx.currentWindow
        )
        val currentFocusedFile = fileEditorManagerEx.currentWindow?.selectedFile
        HTMLEditorProvider.openEditor(
            project,
            PAGE_TITLE,
            PAGE_URL,
            null
        )
        currentFocusedFile?.let {
            fileEditorManagerEx.currentWindow?.closeFile(it)
        }
        markPageAsShown()
    }

    @JvmStatic
    fun isPageShown(): Boolean {
        return PropertiesComponent.getInstance().getBoolean(IS_GETTING_STARTED_OPENED)
    }

    @JvmStatic
    fun markPageAsShown() {
        PropertiesComponent.getInstance().setValue(IS_GETTING_STARTED_OPENED, true)
    }
}
