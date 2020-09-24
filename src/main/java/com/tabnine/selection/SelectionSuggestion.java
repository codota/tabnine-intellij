package com.tabnine.selection;

public class SelectionSuggestion {
    Integer length;
    String strength;
    String origin;

    public SelectionSuggestion() {
    }

    public SelectionSuggestion(Integer length, String strength, String origin) {
        this.length = length;
        this.strength = strength;
        this.origin = origin;
    }
}
