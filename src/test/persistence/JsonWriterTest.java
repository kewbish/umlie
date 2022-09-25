package persistence;

import model.ImportDiagramParser;
import model.JavaInput;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static model.DummyJavaProject.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

// adapted from https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo/blob/master/src/test/persistence/JsonWriterTest.java
public class JsonWriterTest extends JsonTest {
    @Test
    void testWriterInvalidFile() {
        try {
            ImportDiagramParser parser = new ImportDiagramParser();
            JsonWriter writer = new JsonWriter("./data/my\0illegal:fileName.json");
            writer.write(parser);
            fail("IOException was expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testWriterEmptyParser() {
        try {
            ImportDiagramParser parser = new ImportDiagramParser();
            JsonWriter writer = new JsonWriter("./data/tempJsonWriterTest.json");
            writer.write(parser);

            JsonReader reader = new JsonReader("./data/tempJsonWriterTest.json");
            parser = reader.read();
            assertEquals(0, parser.getJavaInputs().size());

            // clean up
            File file = new File("./data/tempJsonWriterTest.json");
            file.delete();
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriterGeneralParser() {
        try {
            ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
            JsonWriter writer = new JsonWriter("./data/tempJsonWriterTestAll.json");
            writer.write(parser);

            JsonReader reader = new JsonReader("./data/tempJsonWriterTestAll.json");
            parser = reader.read();

            List<JavaInput> actual = parser.getJavaInputs();
            assertEquals(5, actual.size());
            checkJavaInput(actual.get(0), FLYER_JAVA);
            checkJavaInput(actual.get(1), PLANE_JAVA);
            checkJavaInput(actual.get(2), SEAGULL_JAVA);
            checkJavaInput(actual.get(3), LAUNCHER_JAVA);
            assertEquals("flyer-app", actual.get(4).getShortName());

            // clean up
            File file = new File("./data/tempJsonWriterTestAll.json");
            file.delete();
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }
}
