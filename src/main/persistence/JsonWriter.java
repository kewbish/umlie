package persistence;

import model.ImportDiagramParser;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

// adapted from https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo/blob/master/src/main/persistence/JsonWriter.java
// writes JSON to a file
public class JsonWriter {
    private static final int TAB_SIZE = 4;
    private PrintWriter writer;
    private final String destination;

    // EFFECTS: constructs writer to write to destination file
    public JsonWriter(String destination) {
        this.destination = destination;
    }

    // MODIFIES: this
    // EFFECTS: opens writer; throws FileNotFoundException if destination file cannot
    // be opened for writing
    private void open() throws FileNotFoundException {
        writer = new PrintWriter(destination);
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of parser to file
    public void write(ImportDiagramParser parser) throws FileNotFoundException {
        open();
        JSONObject json = parser.toJson();
        saveToFile(json.toString(TAB_SIZE));
        close();
    }

    // MODIFIES: this
    // EFFECTS: closes writer
    private void close() {
        writer.close();
    }

    // MODIFIES: this
    // EFFECTS: writes string to file
    private void saveToFile(String json) {
        writer.print(json);
    }
}
