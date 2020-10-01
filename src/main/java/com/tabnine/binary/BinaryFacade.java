package com.tabnine.binary;

import com.tabnine.exceptions.TabNineDeadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryFacade {
    private final BinaryRun binaryRun;

    private Process process = null;
    private BufferedReader reader = null;
    private final AtomicInteger counter = new AtomicInteger(1);

    public BinaryFacade(BinaryRun binaryRun) {
        this.binaryRun = binaryRun;
    }

    public void create() throws IOException, NoValidBinaryToRunException {
        binaryRun.init();
        runBinary();
    }

    public void restart() throws IOException {
        this.runBinary();
    }

    private void runBinary() throws IOException {
        List<String> command = binaryRun.getBinaryRunCommand();
        Process createdProcess = new ProcessBuilder(command).start();

        process = createdProcess;
        reader = new BufferedReader(new InputStreamReader(createdProcess.getInputStream(), StandardCharsets.UTF_8));
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
        return counter.getAndIncrement();
    }
}
