package persistence;

import model.UMLangGenerator;
import model.DiagramType;

import java.io.FileWriter;
import java.io.IOException;

// writes UML files (outputs of UMLangGenerator) to disk
public class UMLangWriter {
    private final String destination;
    private final UMLangGenerator generator;

    // EFFECTS: constructs a UML writer with a filepath destination and the current UML diagram generator
    public UMLangWriter(String destination, UMLangGenerator generator) {
        this.destination = destination;
        this.generator = generator;
    }

    // EFFECTS: writes the current diagram as UML to a file on disk
    public void writeUML(DiagramType diagramType) throws IOException {
        FileWriter fileWriter = new FileWriter(destination);
        fileWriter.write(generator.generateDiagramForType(diagramType));
        fileWriter.close();
    }
}
