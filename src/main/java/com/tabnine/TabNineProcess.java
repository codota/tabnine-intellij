package com.tabnine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.tabnine.Utils.emptyUponException;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

class TabNineProcess {
    public static final String BINARY_PROTOCOL_VERSION = "2.0.2";

    public TabNineProcess() throws IOException {
        TabNineProcessFactory.create(generateCommand());
    }

    public <T> T request(Request<T> request) {
        Gson gson = new GsonBuilder().create();
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("version", BINARY_PROTOCOL_VERSION);
        jsonObject.put("request", singletonMap(request.name(), request));
        String serializedRequest = gson.toJson(jsonObject) + "\n";
        Optional<T> result = this.communicateLine(serializedRequest)
                .flatMap(response -> emptyUponException(() -> gson.fromJson(response, request.response())))
                .filter(request::validate);

        // Result would be null if:
        // 1. Process's dead.
        // 2. Process's BufferedReader has reached its end (also mean dead...).
        // 3. There was an IOException communicating to the process.
        // 4. The result from the process was invalid.
        if (!result.isPresent()) {
            try {
                this.restartBinary();
            } catch (IOException e) {
                Logger.getInstance(getClass()).error("Error restarting TabNine: " + e);
            }
        }

        return result.orElse(null);
    }

    private void restartBinary() throws IOException {
        TabNineProcessFactory.reset();
        TabNineProcessFactory.create(generateCommand());
    }

    @NotNull
    private List<String> generateCommand() throws IOException {
        // When we tell TabNine that it's talking to IntelliJ, it won't suggest language server
        // setup since we assume it's already built into the IDE
        List<String> command = new ArrayList<>(singletonList(TabNineFinder.getTabNinePath()));
        List<String> metadata = new ArrayList<>();
        metadata.add("--client-metadata");
        metadata.add("pluginVersion=" + Utils.getPluginVersion());
        metadata.add("clientIsUltimate=" + PlatformUtils.isIdeaUltimate());
        final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        if (applicationInfo != null) {
            command.add("--client");
            command.add(applicationInfo.getVersionName());
            command.add("--no-lsp");
            command.add("true");
            metadata.add("clientVersion=" + applicationInfo.getFullVersion());
            metadata.add("clientApiVersion=" + applicationInfo.getApiVersion());
        }
        command.addAll(metadata);

        return command;
    }

    private Optional<String> communicateLine(String request) {
        synchronized (this) {
            try {
                if (this.isDead()) {
                    Logger.getInstance(getClass()).info("TabNine cannot respond to the request because the process is dead.");

                    throw new TabNineDeadException();
                }

                synchronized (this) {
                    sendRequest(request);

                    return readResponse();
                }
            } catch (IOException | TabNineDeadException e) {
                Logger.getInstance(getClass()).info("Exception communicating with TabNine!", e);

                return Optional.empty();
            }
        }

    }

    private boolean isDead() {
        return TabNineProcessFactory.getProcess() == null || !TabNineProcessFactory.getProcess().isAlive();
    }

    private Optional<String> readResponse() throws IOException {
        return Optional.ofNullable(TabNineProcessFactory.getReader().readLine());
    }

    private void sendRequest(String request) throws IOException {
        TabNineProcessFactory.getWriter().write(request.getBytes(StandardCharsets.UTF_8));
        TabNineProcessFactory.getWriter().flush();
    }
}
