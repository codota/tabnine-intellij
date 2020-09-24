package com.tabnine.integration;

public class TestData {
    public static final String A_REQUEST_TO_TABNINE_BINARY = "{\"request\":{\"Autocomplete\":{\"before\":\"hello\",\"after\":\"\\nhello\",\"filename\":\"/src/test.txt\",\"region_includes_beginning\":true,\"region_includes_end\":true,\"max_num_results\":5,\"correlation_id\":1}},\"version\":\"2.0.2\"}\n";
    public static final String A_TEST_TXT_FILE = "test.txt";
    public static final String SOME_CONTENT = "hello<caret>\nhello";
    public static final String A_PREDICTION_RESULT = "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"\\\\n return result\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\"11%\"},{\"new_prefix\":\"\\\\n return result;\\\\n\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\" 7%\"}],\"user_message\":[],\"docs\":[],\"correlation_id\":1}";
    public static final String SECOND_PREDICTION_RESULT = "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"test\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\"11%\"}],\"user_message\":[],\"docs\":[],\"correlation_id\":2}";

    public static final int OVERFLOW = 1;
    public static final String INVALID_RESULT = "Nonsense";
}
