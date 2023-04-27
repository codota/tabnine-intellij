package com.tabnine.testUtils;

import static com.tabnineCommon.general.StaticConfig.BINARY_PROTOCOL_VERSION;
import static java.util.stream.Collectors.toList;

import com.tabnineCommon.binary.fetch.BinaryVersion;
import com.tabnineCommon.binary.requests.autocomplete.CompletionMetadata;
import com.tabnineCommon.general.CompletionOrigin;
import java.util.List;
import java.util.stream.Stream;

public class TestData {
  public static final String A_REQUEST_TO_TABNINE_BINARY =
      "{\"request\":{\"Autocomplete\":{\"before\":\"hello\",\"after\":\"\\nhello\",\"filename\":\"/src/test.txt\",\"region_includes_beginning\":true,\"region_includes_end\":true,\"max_num_results\":5,\"offset\":5,\"line\":0,\"character\":5,\"indentation_size\":4}},\"version\":\""
          + BINARY_PROTOCOL_VERSION
          + "\"}\n";
  public static final String A_TEST_TXT_FILE = "test.txt";
  public static final String A_TEST_TXT_FILE_FULL_PATH = "/src/test.txt";
  public static final String A_FILE_WITH_NO_EXTENSION = "file_with_no_extension";
  public static final String SOME_CONTENT = "hello<caret>\nhello";
  public static final String A_PREDICTION_RESULT =
      "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"return result\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\"11%\"}},{\"new_prefix\":\"return result;\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\" 7%\"}}],\"user_message\":[],\"docs\":[]}";
  public static final String SECOND_PREDICTION_RESULT =
      "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"test\",\"old_suffix\":\"\\\\n\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\"11%\"}}],\"user_message\":[],\"docs\":[]}";
  public static final String THIRD_PREDICTION_RESULT =
      "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"temp\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\"21%\"}},{\"new_prefix\":\"temporary\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\" 17%\"}},{\"new_prefix\":\"temporary file\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\" 13%\"}}],\"user_message\":[],\"docs\":[]}";
  public static final String MULTI_LINE_SNIPPET_PREDICTION_RESULT =
      "{\"old_prefix\":\"\",\"results\":[{\"new_prefix\":\"temp\\ntemp2\",\"old_suffix\":\"hello\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\"21%\",\"completion_kind\":\"Snippet\"}},{\"new_prefix\":\"temporary\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\" 17%\"}},{\"new_prefix\":\"temporary file\",\"old_suffix\":\"\",\"new_suffix\":\"\",\"completion_metadata\":{\"origin\":\"LOCAL\",\"detail\":\" 13%\"}}],\"user_message\":[],\"docs\":[]}";
  public static final CompletionMetadata A_COMPLETION_METADATA =
      new CompletionMetadata(CompletionOrigin.LOCAL, "21%", null, null, null, null);
  public static final int OVERFLOW = 1;
  public static final String INVALID_RESULT = "Nonsense";

  public static final String A_NON_EXISTING_BINARY_PATH = "test/kaki/pipi";
  public static final String ANOTHER_VERSION = "1.0.0";
  public static final String A_VERSION = "0.0.1";
  public static final String A_SERVER_URL = "https://kaki.pipi";
  public static final String PREFERRED_VERSION = "2.3.1";
  public static final String PRELATEST_VERSION = "2.20.100";
  public static final String LATEST_VERSION = "2.30.0";
  public static final String BETA_VERSION = "3.0.0";

  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final byte[] A_BINARY_CONTENT = "A_BINARY_CONTENT".getBytes();

  public static final int EPSILON = 10;
  public static final String NONE_EXISTING_SERVICE = "http://localhost:10101/";

  public static final String SET_STATE_REQUEST =
      "{\"request\":{\"SetState\":{\"state_type\":{\"Selection\":{\"language\":\"txt\",\"length\":13,\"origin\":\"LOCAL\",\"net_length\":13,\"strength\":\"11%\",\"index\":1,\"line_prefix_length\":5,\"line_net_prefix_length\":5,\"line_suffix_length\":0,\"num_of_suggestions\":2,\"num_of_vanilla_suggestions\":0,\"num_of_deep_local_suggestions\":2,\"num_of_deep_cloud_suggestions\":0,\"num_of_lsp_suggestions\":0,\"suggestions\":[{\"length\":13,\"strength\":\"11%\",\"origin\":\"LOCAL\"},{\"length\":14,\"strength\":\" 7%\",\"origin\":\"LOCAL\"}],\"suggestion_rendering_mode\":\"Popup\"}}}},\"version\":\""
          + BINARY_PROTOCOL_VERSION
          + "\"}\n";
  public static final String NO_EXTENSION_STATE_REQUEST =
      "{\"request\":{\"SetState\":{\"state_type\":{\"Selection\":{\"language\":\"undefined\",\"length\":13,\"origin\":\"LOCAL\",\"net_length\":13,\"strength\":\"11%\",\"index\":1,\"line_prefix_length\":5,\"line_net_prefix_length\":5,\"line_suffix_length\":0,\"num_of_suggestions\":2,\"num_of_vanilla_suggestions\":0,\"num_of_deep_local_suggestions\":2,\"num_of_deep_cloud_suggestions\":0,\"num_of_lsp_suggestions\":0,\"suggestions\":[{\"length\":13,\"strength\":\"11%\",\"origin\":\"LOCAL\"},{\"length\":14,\"strength\":\" 7%\",\"origin\":\"LOCAL\"}],\"suggestion_rendering_mode\":\"Popup\"}}}},\"version\":\""
          + BINARY_PROTOCOL_VERSION
          + "\"}\n";
  public static final String SET_STATE_RESPONSE = "{\"result\":\"Done\"}";
  public static final String NULL_RESULT = "null";
  public static final String A_COMMAND = "testing";

  public static byte[] binaryContentSized(int size) {
    return new byte[size];
  }

  public static List<BinaryVersion> versions(String... versions) {
    return Stream.of(versions).map(BinaryVersion::new).collect(toList());
  }

  public static List<BinaryVersion> versionsWithBeta() {
    return versions(
        BETA_VERSION,
        LATEST_VERSION,
        PRELATEST_VERSION,
        PREFERRED_VERSION,
        ANOTHER_VERSION,
        A_VERSION);
  }

  public static List<BinaryVersion> aVersions() {
    return versions(
        LATEST_VERSION, PRELATEST_VERSION, PREFERRED_VERSION, ANOTHER_VERSION, A_VERSION);
  }
}
