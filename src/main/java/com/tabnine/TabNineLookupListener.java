package com.tabnine;

import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.LookupListener;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.tabnine.binary.TabNineGateway;
import com.tabnine.binary.TabnineGatewayWrapper;
import com.tabnine.prediction.TabNineLookupElement;
import com.tabnine.selection.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;

import static java.util.Arrays.stream;

public class TabNineLookupListener implements LookupListener {
    private static final long FILE_NAME = 1;
    public static final int THE_FIRST_AND_THE_LAST = 2;
    private static TabNineLookupListener INSTANCE = null;
    private TabNineGateway gateway = TabnineGatewayWrapper.getOrCreateInstance();

    public static synchronized TabNineLookupListener getOrCreate() {
        if (INSTANCE == null) {
            INSTANCE = new TabNineLookupListener();
        }

        return INSTANCE;
    }

    @Override
    public void itemSelected(@NotNull LookupEvent event) {
        if (event.isCanceledExplicitly()) {
            return;
        }

        if (event.getItem() instanceof TabNineLookupElement) {
            // They picked us, yay!
            SelectionRequest selection = new SelectionRequest();

            selection.language = asLanguage(event.getLookup().getPsiFile().getName());
            ((TabNineLookupElement) event.getItem()).newSuffix
            selection.index = ((LookupImpl) event.getLookup()).getSelectedIndex();
            selection.origin = ((TabNineLookupElement) event.getItem()).origin;
            // TODO: Not sure about that
            selection.length = ((TabNineLookupElement) event.getItem()).newPrefix.length();
            // TODO: Fill selection here...
            SetStateBinaryRequest setStateBinaryRequest = new SetStateBinaryRequest(
                    new SetStateRequest(
                            new SetStateRequestContent(
                                    new SetStateRequestContentInner(
                                            selection
                                    )
                            )
                    )
            );

            gateway.request(setStateBinaryRequest);
        }
    }

    @NotNull
    private String asLanguage(String name) {
        String[] split = name.split(".");

        return stream(split).skip(Math.min(1, split.length - 1)).findAny().orElse("UNKOWN");
    }
}
