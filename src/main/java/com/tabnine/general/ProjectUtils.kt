package com.tabnine.general

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import java.util.Arrays
import java.util.Optional

fun getFocusedProject(): Optional<Project> {
    return Arrays.stream(ProjectManager.getInstance().openProjects)
        .filter() {
            WindowManager.getInstance().getFrame(it)?.isFocused == true
        }
        .findFirst()
}
