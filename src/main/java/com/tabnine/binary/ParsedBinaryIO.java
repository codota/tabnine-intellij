package com.tabnine.binary;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tabnine.binary.exceptions.TabNineDeadException;
import com.tabnine.binary.exceptions.TabNineInvalidResponseException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

public class ParsedBinaryIO {
    private final Gson gson;
    private final BinaryProcessGateway binaryProcessGateway;

    public ParsedBinaryIO(Gson gson, BinaryProcessGateway binaryProcessGateway) {
        this.gson = gson;
        this.binaryProcessGateway = binaryProcessGateway;
    }

    @Nonnull
    public <R> R readResponse(Class<R> responseClass) throws IOException, TabNineDeadException, TabNineInvalidResponseException {
        String rawResponse = binaryProcessGateway.readRawResponse();

        try {
            return Optional.ofNullable(gson.fromJson(rawResponse, responseClass))
                    .orElseThrow(() -> new TabNineInvalidResponseException("Binary returned null as a response"));
        } catch (TabNineInvalidResponseException | JsonSyntaxException e) {
            throw new TabNineInvalidResponseException(format("Binary returned illegal response: %s", rawResponse), e, rawResponse);
        }
    }

    public void writeRequest(Object request) throws IOException {
        binaryProcessGateway.writeRequest(gson.toJson(request) + "\n");
    }

    public boolean isDead() {
        return binaryProcessGateway.isDead();
    }
}
