package com.tabnineCommon.general;

import com.google.gson.annotations.SerializedName;

public enum ServiceLevel {
  @SerializedName("Free")
  FREE,
  @SerializedName("Pro")
  PRO,
  @SerializedName("Trial")
  TRIAL,
  @SerializedName("Business")
  BUSINESS,
}
