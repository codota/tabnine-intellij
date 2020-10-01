package com.tabnine.binary;

import com.tabnine.exceptions.TabNineDeadException;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TabNineProcessFacade {
    private static TabNineBinary tabNineBinary = new TabNineBinary();

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
    public static String readLine() throws IOException, TabNineDeadException {
        return tabNineBinary.readRawResponse();
    }

    public static void writeRequest(String request) throws IOException {
        tabNineBinary.writeRequest(request);
    }

    public static boolean isDead() {
        return tabNineBinary.isDead();
    }

    public static int getAndIncrementCorrelationId() {
        return tabNineBinary.getAndIncrementCorrelationId();
    }
}
