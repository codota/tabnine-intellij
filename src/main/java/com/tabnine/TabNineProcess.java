package com.tabnine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.application.ApplicationInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class TabNineProcess {
    Process proc;
    BufferedReader procLineReader;
    int restartCount = 0;

    TabNineProcess() throws IOException {
        this.startTabNine();
    }

    void startTabNine() throws IOException {
        if (this.proc != null) {
            this.proc.destroy();
            this.proc = null;
        }
        // When we tell TabNine that it's talking to IntelliJ, it won't suggest language server
        // setup since we assume it's already built into the IDE
        ProcessBuilder builder = new ProcessBuilder(TabNineFinder.getTabNinePath(), "--client", ApplicationInfo.getInstance().getVersionName());
        this.proc = builder.start();
        this.procLineReader = new BufferedReader(new InputStreamReader(this.proc.getInputStream(), StandardCharsets.UTF_8));
    }

    void restartTabNine(boolean checkCount) throws IOException {
        if (checkCount) {
            if (this.restartCount >= 5) {
                return;
            }
            ++this.restartCount;
        }
        this.startTabNine();
    }

    <T> T request(Request<T> r) {
        Gson gson = new GsonBuilder().create();
        Map<String, Object> jsonObject = new HashMap<>();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(r.name(), r);
        jsonObject.put("version", "2.0.2");
        jsonObject.put("request", requestMap);
        String rJson = gson.toJson(jsonObject) + "\n";
        String responseJson = this.communicateLine(rJson);
        if (responseJson == null) {
            return null;
        } else {
            T response = gson.fromJson(responseJson, r.response());
            if (r.validate(response)) {
                return response;
            } else {
                try {
                    this.restartTabNine(false);
                } catch (IOException e) {
                    Logger.getInstance(getClass()).error("Error restarting TabNine: " + e);
                }
                return null;
            }
        }
    }

    boolean isDead() {
        return this.proc == null;
    }

    String communicateLine(String req) {
        synchronized (this) {
            if (this.isDead()) {
                Logger.getInstance(getClass()).info("TabNine cannot respond to the request because the process is dead.");
                return null;
            }
            try {
                byte[] toWrite = req.getBytes(StandardCharsets.UTF_8);
                synchronized (this) {
                    this.proc.getOutputStream().write(toWrite);
                    this.proc.getOutputStream().flush();
                    return this.procLineReader.readLine();
                }
            } catch (IOException e) {
                Logger.getInstance(getClass()).info("Exception communicating with TabNine: " + e);
                try {
                    this.restartTabNine(true);
                } catch (IOException e2) {
                    Logger.getInstance(getClass()).error("Error restarting TabNine: " + e2);
                    this.proc = null;
                }
            }
        }
        return null;
    }

    static interface Request<T> {
        String name();
        Class<T> response();
        boolean validate(T response);
    }

    static class AutocompleteRequest implements Request<AutocompleteResponse> {
        String before;
        String after;
        String filename;
        boolean region_includes_beginning;
        boolean region_includes_end;
        int max_num_results;

        public String name() {
            return "Autocomplete";
        }

        public Class<AutocompleteResponse> response() {
            return AutocompleteResponse.class;
        }

        public boolean validate(AutocompleteResponse response) {
            return this.before.endsWith(response.old_prefix);
        }
    }

    static class AutocompleteResponse {
        String old_prefix;
        ResultEntry[] results;
        String[] user_message;
    }

    static class ResultEntry {
        String new_prefix;
        String old_suffix;
        String new_suffix;

        String detail;
        Boolean deprecated;
        // TODO other lsp types
    }
}
