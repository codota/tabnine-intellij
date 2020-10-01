package com.tabnine.binary;

import com.tabnine.DependencyContainer;
import com.tabnine.exceptions.TabNineDeadException;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TabNineProcessFacade {
    private static BinaryFacade binaryFacade = DependencyContainer.instanceOfBinaryFacade();

    public static void setTesting(BinaryFacade tabNineBinary) {
        TabNineProcessFacade.binaryFacade = tabNineBinary;
    }

    public static void create() throws IOException, NoValidBinaryToRunException {
        binaryFacade.create();
    }

    public static void restart() throws IOException, NoValidBinaryToRunException {
        binaryFacade.restart();
    }

    @Nonnull
    public static String readLine() throws IOException, TabNineDeadException {
        return binaryFacade.readRawResponse();
    }

    public static void writeRequest(String request) throws IOException {
        binaryFacade.writeRequest(request);
    }

    public static boolean isDead() {
        return binaryFacade.isDead();
    }

    public static int getAndIncrementCorrelationId() {
        return binaryFacade.getAndIncrementCorrelationId();
    }
}
