package persistence;

import model.JavaInput;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {
    public void checkJavaInput(JavaInput javaInput, JavaInput expected) {
        assertEquals(expected.getShortName(),
                javaInput.getShortName());
        assertEquals(expected.getJavaContents().replaceAll("\\s+", ""),
                javaInput.getJavaContents().replaceAll("\\s+", ""));
    }
}
