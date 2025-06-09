package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegexTest {

    @Test
    void testNormal() {
        assertEquals("/home tp default"
            , "/home".replaceAll("/home", "/home tp default")
        );
    }

    @Test
    void testRef() {
        assertEquals("/aid alice"
            , "/help alice".replaceAll("/help (.*)", "/aid $1")
        );
    }

    @SuppressWarnings("ReplaceOnLiteralHasNoEffect")
    @Test
    void testNoEffect() {
        assertEquals("/first alice"
            , "/first alice".replaceAll("/help (.*)", "/aid $1")
        );

    }
}
