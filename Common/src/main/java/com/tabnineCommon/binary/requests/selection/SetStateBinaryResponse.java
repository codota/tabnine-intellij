package com.tabnineCommon.binary.requests.selection;

import com.tabnineCommon.binary.BinaryResponse;

public class SetStateBinaryResponse implements BinaryResponse {
  private String result;

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
