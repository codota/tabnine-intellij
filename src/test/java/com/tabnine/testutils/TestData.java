package com.tabnine.testutils;

import java.util.List;

import static java.util.Arrays.asList;

public class TestData {
    public static final String A_REQUEST_TO_TABNINE_BINARY = "{\"request\":{\"Autocomplete\":{\"before\":\"hello\",\"after\":\"\\nhello\",\"filename\":\"/src/test.txt\",\"region_includes_beginning\":true,\"region_includes_end\":true,\"max_num_results\":5,\"correlation_id\":1}},\"version\":\"2.0.2\"}\n";
    public static final String A_TEST_TXT_FILE = "test.txt";
    public static final String SOME_CONTENT = "hello<caret>\nhello";
    public static final String A_PREDICTION_RESULT = "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"\\\\n return result\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\"11%\"},{\"new_prefix\":\"\\\\n return result;\\\\n\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\" 7%\"}],\"user_message\":[],\"docs\":[],\"correlation_id\":1}";
    public static final String SECOND_PREDICTION_RESULT = "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"test\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"origin\":\"LOCAL\",\"detail\":\"11%\"}],\"user_message\":[],\"docs\":[],\"correlation_id\":2}";

    public static final int OVERFLOW = 1;
    public static final String INVALID_RESULT = "Nonsense";

    public static final String A_NON_EXISTING_BINARY_PATH = "test/kaki/pipi";
    public static final String ANOTHER_VERSION = "1.0.0";
    public static final String A_VERSION = "0.0.1";
    public static final String PREFERRED_VERSION = "2.3.1";
    public static final String PRELATEST_VERSION = "2.20.100";
    public static final String LATEST_VERSION = "2.30.0";
    public static final String BETA_VERSION = "3.0.0";
    public static final List<String> VERSIONS_LIST = asList(A_VERSION, ANOTHER_VERSION, PREFERRED_VERSION, PRELATEST_VERSION, LATEST_VERSION);
    public static final List<String> VERSIONS_LIST_WITH_BETA_VERSION = asList(A_VERSION, ANOTHER_VERSION, PREFERRED_VERSION, PRELATEST_VERSION, LATEST_VERSION, BETA_VERSION);

    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final byte[] A_BINARY_CONTENT = "A_BINARY_CONTENT".getBytes();

    public static final int EPSILON = 1;
    public static final String NONE_EXISTING_SERVICE = "http://localhost:10101/";

    public static byte[] binaryContentSized(int size) {
        return new byte[size];
    }
}
