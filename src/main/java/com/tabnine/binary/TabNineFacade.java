package com.tabnine.binary;

import com.tabnine.exceptions.TabNineDeadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.tabnine.StaticConfig.generateCommand;

public class TabNineFacade {
    private static Process process = null;
    private static BufferedReader reader = null;
    private volatile int counter = 1;

    public void create() throws IOException {
        List<String> command = generateCommand();
        Process createdProcess = new ProcessBuilder(command).start();

        process = createdProcess;
        reader = new BufferedReader(new InputStreamReader(createdProcess.getInputStream(), StandardCharsets.UTF_8));
    }

    public void restart() throws IOException {
        this.create();
    }

    public String readRawResponse() throws IOException, TabNineDeadException {
        return Optional.ofNullable(reader.readLine())
                .orElseThrow(() -> new TabNineDeadException("End of stream reached"));
    }

    public void writeRequest(String request) throws IOException {
        OutputStream outputStream = process.getOutputStream();

        outputStream.write(request.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    public boolean isDead() {
        return process == null || !process.isAlive();
    }

    public synchronized int getAndIncrementCorrelationId() {
        return counter++;
    }
}
