package com.tabnine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TabNineProcessFactory {
    private static Process process = null;
    private static BufferedReader reader = null;

    public static void setProcessForTesting(Process process, BufferedReader reader) {
        TabNineProcessFactory.process = process;
        TabNineProcessFactory.reader = reader;
    }

    public static void create(List<String> command) throws IOException {
        if (process != null && reader != null) {
            return;
        }

        Process createdProcess = new ProcessBuilder(command).start();

        process = createdProcess;
        reader = new BufferedReader(new InputStreamReader(createdProcess.getInputStream(), StandardCharsets.UTF_8));
    }

    public static void reset() {
        if (process != null) {
            process.destroy();
            process = null;
            reader = null;
        }
    }

    public static BufferedReader getReader() {
        return reader;
    }

    public static OutputStream getWriter() {
        return process.getOutputStream();
    }

    public static Process getProcess() {
        return process;
    }
}
