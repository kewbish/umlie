package persistence;

import model.ImportDiagramParser;
import model.JavaInput;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

// adapted from https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo/blob/master/src/main/persistence/JsonReader.java
// reads JSON from a file and loads it into the parser
public class JsonReader {
    private final String source;

    // EFFECTS: constructs reader to read from source file
    public JsonReader(String source) {
        this.source = source;
    }

    // EFFECTS: reads parser from file and returns it;
    // throws IOException if an error occurs reading data from file
    public ImportDiagramParser read() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseParser(jsonObject);
    }

    // EFFECTS: reads source file as string and returns it
    private String readFile(String source) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(source), StandardCharsets.UTF_8)) {
            stream.forEach(contentBuilder::append);
        }

        return contentBuilder.toString();
    }

    // EFFECTS: parses diagram parser from JSON object and returns it
    private ImportDiagramParser parseParser(JSONObject jsonObject) {
        ImportDiagramParser parser = new ImportDiagramParser();
        addJavaInputs(parser, jsonObject);
        return parser;
    }

    // MODIFIES: parser
    // EFFECTS: parses JavaInputs from JSON array and adds them to parser
    private void addJavaInputs(ImportDiagramParser parser, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("javaInputs");
        for (Object json : jsonArray) {
            JSONObject nextThingy = (JSONObject) json;
            addJavaInput(parser, nextThingy);
        }
    }

    // MODIFIES: parser
    // EFFECTS: parses JavaInput from JSON object and adds it to workroom
    private void addJavaInput(ImportDiagramParser parser, JSONObject jsonObject) {
        JavaInput javaInput = new JavaInput(jsonObject.getString("shortName"), jsonObject.getString("contents"));
        parser.addJavaInput(javaInput);
    }
}
