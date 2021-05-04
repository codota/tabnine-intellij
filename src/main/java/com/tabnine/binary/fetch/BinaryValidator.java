package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

public class BinaryValidator {
    public boolean isWorking(String binaryFullPath) {
        File binaryFile = new File(binaryFullPath);

        // we test only absolute paths because otherwise we would have to search $PATH
        // which java has no built in support for
        if (binaryFile.isAbsolute() && !binaryFile.exists()) {
            return false;
        }

        ProcessBuilder binary = new ProcessBuilder(binaryFullPath, "--print-version");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.start().getInputStream(), StandardCharsets.UTF_8))) {
            if (reader.readLine() != null) {
                return true;
            }
        } catch (Exception e) {
            Logger.getInstance(getClass()).warn(format("Tabnine binary at `%s` was queried for it's version and failed to respond.", binaryFullPath), e);
        }

        return false;
    }
}
