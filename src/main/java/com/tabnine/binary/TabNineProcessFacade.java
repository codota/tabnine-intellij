package com.tabnine.binary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tabnine.exceptions.TabNineDeadException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

public class TabNineProcessFacade {
    private static TabNineBinary tabNineBinary = new TabNineBinary();
    private static final Gson GSON = new GsonBuilder().create();

    public static void setTesting(TabNineBinary tabNineBinary) {
        TabNineProcessFacade.tabNineBinary = tabNineBinary;
    }

    public static void create() throws IOException {
        tabNineBinary.create();
    }

    public static void restart() throws IOException {
        tabNineBinary.restart();
    }

    @Nonnull
    public static <T> T readLine(Class<T> clazz) throws JsonSyntaxException, IOException, TabNineDeadException {
        return Optional.ofNullable(tabNineBinary.readRawResponse())
                .map(text -> GSON.fromJson(text, clazz))
                .orElseThrow(() -> new TabNineDeadException("End of stream reached"));
    }

    public static <T> void writeRequest(T request) throws IOException {
        tabNineBinary.writeRequest(GSON.toJson(request) + "\n");
    }

    public static boolean isDead() {
        return tabNineBinary.isDead();
    }

    public static int getAndIncrementCorrelationId() {
        return tabNineBinary.getAndIncrementCorrelationId();
    }
}
