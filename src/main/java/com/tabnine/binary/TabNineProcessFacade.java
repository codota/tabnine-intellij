package com.tabnine.binary;

import com.tabnine.exceptions.TabNineDeadException;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TabNineProcessFacade {
    private static TabNineFacade tabNineFacade = new TabNineFacade();

    public static void setTesting(TabNineFacade tabNineFacade) {
        TabNineProcessFacade.tabNineFacade = tabNineFacade;
    }

    public static void create() throws IOException {
        tabNineFacade.create();
    }

    public static void restart() throws IOException {
        tabNineFacade.restart();
    }

    @Nonnull
    public static String readLine() throws IOException, TabNineDeadException {
        return tabNineFacade.readRawResponse();
    }

    public static void writeRequest(String request) throws IOException {
        tabNineFacade.writeRequest(request);
    }

    public static boolean isDead() {
        return tabNineFacade.isDead();
    }

    public static int getAndIncrementCorrelationId() {
        return tabNineFacade.getAndIncrementCorrelationId();
    }
}
