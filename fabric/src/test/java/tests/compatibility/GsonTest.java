package tests.compatibility;

import auxiliary.JavaParserUtil;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GsonTest {

    @Test
    void banTheCallToJavaObjectIsEmpty() {
        JavaParserUtil.banMethodCalls(
            List.of("com.google.gson.JsonObject.isEmpty")
            , List.of()
            , """
                This method only exists in high version Gson library.
                Some target platforms may use an old version Gson, causing NoSuchMethodError thrown.
                """);

    }
}
