package persistence;

import model.ImportDiagramParser;
import model.JavaInput;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static model.DummyJavaProject.*;
import static org.junit.jupiter.api.Assertions.*;

// adapted from https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo/blob/master/src/test/persistence/JsonReaderTest.java
public class JsonReaderTest extends  JsonTest{

    @Test
    public void testReaderNonExistentFile() {
        JsonReader reader = new JsonReader("./data/noSuchFile.json");
        try {
            reader.read();
            fail("IOException expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    public void testReaderEmptyWorkRoom() {
        JsonReader reader = new JsonReader("./data/testReaderEmptyInput.json");
        try {
            ImportDiagramParser parser = reader.read();
            assertEquals(0, parser.getJavaInputs().size());
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

    @Test
    public void testReaderAllJavaInputs() {
        JsonReader reader = new JsonReader("./data/testReaderAllFiles.json");
        try {
            ImportDiagramParser parser = reader.read();
            List<JavaInput> actual = parser.getJavaInputs();
            assertEquals(5, actual.size());
            checkJavaInput(actual.get(0), FLYER_JAVA);
            checkJavaInput(actual.get(1), LAUNCHER_JAVA);
            assertEquals("flyer-app", actual.get(2).getShortName());
            checkJavaInput(actual.get(3), PLANE_JAVA);
            checkJavaInput(actual.get(4), SEAGULL_JAVA);
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }


}