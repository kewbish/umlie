package model;

// wrapper to create a list of possible enum
public enum DiagramType {
    PACKAGE("package"),
    CLASS_IMPORT("imports"),
    CLASS_RELATIONSHIP("relationship");

    private final String label;

    // EFFECTS: instantiates a new instance of the enum (just to associate a given string with the label)
    DiagramType(String s) {
        label = s;
    }
}
