package model;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaInputTest {
    private JavaInput javaInput;
    private static String javaContents;

    @BeforeEach
    public void setup() {
        javaContents =
                "package files.model;\n" + "\n" + "public interface Flyer {\n" + "    public void fly();\n" + "}";
        javaInput = new JavaInput("flyer-interface", javaContents);
    }

    @Test
    public void testJavaInputConstructor() {
        assertEquals("flyer-interface", javaInput.getShortName());
        assertEquals(javaContents, javaInput.getJavaContents());
    }

    @Test
    public void testJavaInputToJson() {
        JSONObject expected = new JSONObject();
        expected.put("shortName", "flyer-interface");
        expected.put("contents", javaContents);
        assertEquals(expected.toString(), javaInput.toJson().toString());
    }
}
