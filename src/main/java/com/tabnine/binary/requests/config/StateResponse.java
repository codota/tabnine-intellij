package com.tabnine.binary.requests.config;

import com.google.gson.annotations.SerializedName;
import com.tabnine.binary.BinaryResponse;

public class StateResponse implements BinaryResponse {
    public enum ServiceLevel {
        @SerializedName("Free")
        FREE,
        @SerializedName("Pro")
        PRO,
    }
    @SerializedName("service_level")
    ServiceLevel serviceLevel;

    public ServiceLevel getServiceLevel() {
        return serviceLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateResponse that = (StateResponse) o;

        return serviceLevel != null ? serviceLevel.equals(that.serviceLevel) : that.serviceLevel == null;
    }

    @Override
    public int hashCode() {
        return serviceLevel != null ? serviceLevel.hashCode() : 0;
    }
}
