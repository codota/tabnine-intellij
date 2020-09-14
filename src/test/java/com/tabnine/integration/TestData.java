package com.tabnine.integration;

import java.nio.charset.StandardCharsets;

public class TestData {
    public static final byte[] A_REQUEST_TO_TABNINE_BINARY = "{\"request\":{\"Autocomplete\":{\"before\":\"hello\",\"after\":\"\\nhello\",\"filename\":\"/src/test.txt\",\"region_includes_beginning\":true,\"region_includes_end\":true,\"max_num_results\":5}},\"version\":\"2.0.2\"}\n".getBytes(StandardCharsets.UTF_8);
    public static final String A_TEST_TXT_FILE = "test.txt";
    public static final String SOME_CONTENT = "hello<caret>\nhello";
    public static final String A_PREDICTION_RESULT = "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"\\\\n return result\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\"11%\"},{\"new_prefix\":\"\\\\n return result;\\\\n\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\" 7%\"}],\"user_message\":[],\"docs\":[]}";
    public static final int OVERFLOW = 1;
}
