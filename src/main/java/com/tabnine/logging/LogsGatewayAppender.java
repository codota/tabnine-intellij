package com.tabnine.logging;

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.PermanentInstallationID;
import com.tabnine.config.Config;
import com.tabnine.general.Utils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;

public class LogsGatewayAppender extends AppenderSkeleton {
    final JsonObject baseRequestBody = buildRequestBody();

    private JsonObject buildRequestBody() {
        JsonObject body = new JsonObject();
        body.addProperty("appName", "tabnine-plugin-JB");
        body.addProperty("category", "extensions");
        body.addProperty("ide", ApplicationInfo.getInstance().getVersionName());
        body.addProperty("ideVersion", ApplicationInfo.getInstance().getFullVersion());
        body.addProperty("pluginVersion", Utils.cmdSanitize(Utils.getTabNinePluginVersion()));
        body.addProperty("os", System.getProperty("os.name"));
        body.addProperty("channel", Config.CHANNEL);
        body.addProperty("userId", PermanentInstallationID.get());
        return body;
    }

    private void dispatchLog(String level, String message, String stackTrace) throws Exception {
        HttpPost postRequest = new HttpPost(String.format(
                "%s/logs/%s",
                Config.LOGGER_HOST,
                level
        ));

        JsonObject requestBody = baseRequestBody.deepCopy();
        requestBody.addProperty("message", message);
        // requestBody.addProperty("stackTrace", stackTrace);

        postRequest.setEntity(new StringEntity(requestBody.toString()));
        postRequest.setHeader("Content-Type", "application/json");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.execute(postRequest);
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Thread(() -> {
            if (loggingEvent.getThrowableInformation() != null) {
                try {
                    dispatchLog(
                            loggingEvent.getLevel().toString().toLowerCase(),
                            loggingEvent.getMessage().toString(),
                            getStackTrace(loggingEvent.getThrowableInformation().getThrowable())
                    );
                } catch (Exception ignored) {
                }
            }
        }));
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
