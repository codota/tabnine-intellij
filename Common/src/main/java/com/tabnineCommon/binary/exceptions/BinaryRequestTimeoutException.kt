package com.tabnineCommon.binary.exceptions

class BinaryRequestTimeoutException : RuntimeException("Requests to TabNine's binary are consistently taking too long. Restarting the binary.")
