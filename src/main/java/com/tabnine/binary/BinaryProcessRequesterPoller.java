package com.tabnine.binary;

import com.tabnine.binary.exceptions.TabNineDeadException;

public interface BinaryProcessRequesterPoller {
  void pollUntilReady(BinaryProcessGateway binaryProcessGateway) throws TabNineDeadException;
}
