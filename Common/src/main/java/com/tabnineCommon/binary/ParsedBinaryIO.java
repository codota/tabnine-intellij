package com.tabnineCommon.binary;

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tabnineCommon.binary.exceptions.TabNineDeadException;
import com.tabnineCommon.binary.exceptions.TabNineInvalidResponseException;
import java.io.IOException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ParsedBinaryIO {
  private final Gson gson;
  private final BinaryProcessGateway binaryProcessGateway;

  public ParsedBinaryIO(Gson gson, BinaryProcessGateway binaryProcessGateway) {
    this.gson = gson;
    this.binaryProcessGateway = binaryProcessGateway;
  }

  @NotNull
  public <R> R readResponse(Class<R> responseClass)
      throws IOException, TabNineDeadException, TabNineInvalidResponseException {
    String rawResponse = binaryProcessGateway.readRawResponse();

    try {
      return Optional.ofNullable(gson.fromJson(rawResponse, responseClass))
          .orElseThrow(
              () ->
                  new TabNineInvalidResponseException(
                      format(
                          "Binary returned null as a response for %s",
                          responseClass.getSimpleName())));
    } catch (TabNineInvalidResponseException | JsonSyntaxException e) {
      throw new TabNineInvalidResponseException(
          format("Binary returned illegal response: %s", rawResponse), e, rawResponse);
    }
  }

  public void writeRequest(Object request) throws IOException {
    binaryProcessGateway.writeRequest(gson.toJson(request) + "\n");
  }

  public Long pid() {
    return binaryProcessGateway.pid();
  }

  public boolean isDead() {
    return binaryProcessGateway.isDead();
  }

  public void destroy() {
    binaryProcessGateway.destroy();
  }
}
