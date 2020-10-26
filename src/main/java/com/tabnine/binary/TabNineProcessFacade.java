package com.tabnine.binary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.tabnine.binary.exceptions.NoValidBinaryToRunException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;
import com.tabnine.general.DependencyContainer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

public class TabNineProcessFacade {
    private static final Gson GSON = new GsonBuilder().create();
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
    public static <R> R readLine(Class<R> responseClass) throws IOException, TabNineDeadException, TabNineInvalidResponseException {
        String rawResponse = binaryFacade.readRawResponse();

        try {
            return Optional.ofNullable(GSON.fromJson(rawResponse, responseClass))
                    .orElseThrow(() -> new TabNineInvalidResponseException("Binary returned null as a response"));
        } catch (TabNineInvalidResponseException | JsonSyntaxException e) {
            throw new TabNineInvalidResponseException(format("Binary returned illegal response: %s", rawResponse), e, rawResponse);
        }
    }

    public static void writeRequest(Object request) throws IOException {
        binaryFacade.writeRequest(GSON.toJson(request) + "\n");
    }


    public static boolean isDead() {
        return binaryFacade.isDead();
    }
}
