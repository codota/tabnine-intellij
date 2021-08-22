package com.tabnine;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

// https://plugins.jetbrains.com/docs/marketplace/intellij-plugin-recommendations.html#file-type
public class TabnineFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(TabnineIgnoreFileType.INSANCE);
        consumer.consume(TabnineRootFileType.INSTANCE);
        consumer.consume(TabnineProjectModelFileType.INSTANCE);
        consumer.consume(TabnineProjectModelFileType.INSTANCE, "tabnine");
        consumer.consume(TabnineProjectModelFileType.INSTANCE, ".tabnine");
        consumer.consume(TabnineProjectModelFileType.INSTANCE, "tabnine.model");
        consumer.consume(TabnineProjectModelFileType.INSTANCE, ".tabnine.model");
        consumer.consume(TabnineProjectModelFileType.INSTANCE, "model");
        consumer.consume(TabnineProjectModelFileType.INSTANCE, ".model");


    }
}
