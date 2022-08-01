package com.tabnine.state;

import static com.tabnine.general.DependencyContainer.instanceOfBinaryRequestFacade;

import com.tabnine.binary.BinaryRequestFacade;
import com.tabnine.binary.requests.config.StateRequest;
import com.tabnine.binary.requests.config.StateResponse;

public class UserState {
  public static UserState userState;
  private final SuggestionHintState suggestionHintState;

  public static void init() {
    if (userState == null) {
      userState = new UserState();
    }
  }

  public static UserState getInstance() {
    init();
    return userState;
  }

  private UserState() {
    BinaryRequestFacade binaryRequestFacade = instanceOfBinaryRequestFacade();
    StateResponse stateResponse = binaryRequestFacade.executeRequest(new StateRequest());
    suggestionHintState = new SuggestionHintState(stateResponse.getInstallationTime());
  }

  public SuggestionHintState getSuggestionHintState() {
    return suggestionHintState;
  }
}
