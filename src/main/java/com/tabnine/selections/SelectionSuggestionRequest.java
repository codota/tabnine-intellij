package com.tabnine.selections;

public class SelectionSuggestionRequest {
    Integer length;
    String strength;
    String origin;

    public SelectionSuggestionRequest() {
    }

    public SelectionSuggestionRequest(Integer length, String strength, String origin) {
        this.length = length;
        this.strength = strength;
        this.origin = origin;
    }
}
