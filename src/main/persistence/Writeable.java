package persistence;

import org.json.JSONObject;

// taken from https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo/blob/master/src/main/persistence/Writable.java
// implemented by objects that can be written to JSON / files
public interface Writeable {

    // EFFECTS: returns this as JSON object
    JSONObject toJson();
}