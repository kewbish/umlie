package model;

import org.json.JSONObject;
import persistence.Writeable;

// wrapper class to associate a shortname with the contents of a Java file
public class JavaInput implements Writeable {
    private final String shortName;
    private final String javaContents;

    // REQUIRES: javaContents is valid Java code
    // EFFECTS: instantiates an input object with the given nickname and contents
    public JavaInput(String shortName, String javaContents) {
        this.shortName = shortName;
        this.javaContents = javaContents;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("shortName", shortName);
        json.put("contents", javaContents);
        return json;
    }

    // getters

    public String getShortName() {
        return shortName;
    }

    public String getJavaContents() {
        return javaContents;
    }
}
