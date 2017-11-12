package conTest;

import java.io.File;

public interface SharedConstants
{
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") public static final String LS = System.lineSeparator();
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") public static final String FS = File.separator;

    static final String CDT_PREFIX = "cdt";
    static final String CDT_TRIGGER_FUNCTION_NAME = CDT_PREFIX + "_trigger";
    static final String CDT_TRIGGER_FUNCTION_LOG_PATH =
        "os.path.join(os.path.expanduser(\"~\"), \"." + CDT_PREFIX + "_client.log\")";

    static final String CDT_TEST_FILE_PATH = "cdtTestFilePath";
}
