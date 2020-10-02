package com.tabnine.binary.fetch;

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

public class BinaryValidator {
    public boolean isWorking(String binaryFullPath) {
        ProcessBuilder binary = new ProcessBuilder(binaryFullPath, "--print-version");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(binary.start().getInputStream(), StandardCharsets.UTF_8))) {
            if (reader.readLine() != null) {
                return true;
            }
        } catch (Exception e) {
            Logger.getInstance(getClass()).warn(format("TabNine binary at `%s` was queried for it's version and failed to respond.", binaryFullPath), e);
        }

        return false;
    }
}
