package com.tabnine.inline.render;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.tabnine.capabilities.CapabilitiesService;
import com.tabnine.capabilities.Capability;
import com.tabnine.inline.render.experimental.ExperimentalTabnineInlay;
import com.tabnine.inline.render.preserved.DefaultTabnineInlay;
import com.tabnine.prediction.TabNineCompletion;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface TabnineInlay {
    Integer getOffset();
    Rectangle getBounds();
    boolean isEmpty();
    void register(Disposable parent);
    void clear();
    void render(Editor editor, @NotNull String suffix, TabNineCompletion completion, int offset);

    static TabnineInlay create() {
        if (CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)) {
            return new ExperimentalTabnineInlay();
        }

        return new DefaultTabnineInlay();
    }
}
