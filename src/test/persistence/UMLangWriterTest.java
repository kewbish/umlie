package persistence;


import model.DiagramType;
import model.ImportDiagramParser;
import model.UMLangGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static model.DummyJavaProject.javaInputs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class UMLangWriterTest {
    @Test
    public void testUMLangWriterEmptyParser() {
        UMLangWriter umLangWriter = new UMLangWriter("./data/tempUMLangWriterEmpty.uml",
                new UMLangGenerator(new ImportDiagramParser()));
        try {
            umLangWriter.writeUML(DiagramType.CLASS_IMPORT);
            assertEquals("@startuml\n@enduml", getContentsFromPath("./data/tempUMLangWriterEmpty.uml"));
        } catch (Exception e) {
            fail();
        }

        File file = new File("./data/tempUMLangWriterEmpty.uml");
        file.delete();
    }

    @Test
    public void testUMLangWriterInvalidPath() {
        UMLangWriter umLangWriter = new UMLangWriter("./data/temp\0UMLangWriterInvalidPath.uml",
                new UMLangGenerator(new ImportDiagramParser()));
        try {
            umLangWriter.writeUML(DiagramType.CLASS_IMPORT);
            fail();
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    public void testUMLangWriterPackage() {
        UMLangWriter umLangWriter = new UMLangWriter("./data/tempUMLangWriterAllPackage.uml",
                new UMLangGenerator(new ImportDiagramParser(javaInputs)));
        try {
            umLangWriter.writeUML(DiagramType.PACKAGE);
            assertEquals("@startuml\nfolder model\nfolder ui\nui -> model\n@enduml",
                    getContentsFromPath("./data/tempUMLangWriterAllPackage.uml"));
        } catch (Exception e) {
            fail();
        }

        File file = new File("./data/tempUMLangWriterAllPackage.uml");
        file.delete();
    }

    @Test
    public void testUMLangWriterRelationship() {
        UMLangWriter umLangWriter = new UMLangWriter("./data/tempUMLangWriterAllRelationship.uml",
                new UMLangGenerator(new ImportDiagramParser(javaInputs)));
        try {
            umLangWriter.writeUML(DiagramType.CLASS_RELATIONSHIP);
            assertEquals(
                    "@startuml\n"
                            + "interface model.Flyer\n"
                            + "class model.Plane\n"
                            + "model.Plane ..|> model.Flyer\n"
                            + "class model.Seagull\n"
                            + "model.Seagull ..|> model.Flyer\n"
                            + "class model.Launcher\n"
                            + "class ui.FlyerApp\n"
                            + "ui.FlyerApp -.-> model.Seagull\n"
                            + "ui.FlyerApp -.-> model.Plane\n"
                            + "ui.FlyerApp -.-> model.Launcher\n"
                            + "ui.FlyerApp -.-> model.Flyer\n"
                            + "@enduml",
                    getContentsFromPath("./data/tempUMLangWriterAllRelationship.uml"));
        } catch (Exception e) {
            fail();
        }

        File file = new File("./data/tempUMLangWriterAllRelationship.uml");
        file.delete();
    }

    @Test
    public void testUMLangWriterImport() {
        UMLangWriter umLangWriter = new UMLangWriter("./data/tempUMLangWriterAllImport.uml",
                new UMLangGenerator(new ImportDiagramParser(javaInputs)));
        try {
            umLangWriter.writeUML(DiagramType.CLASS_IMPORT);
            assertEquals(
                    "@startuml\n" + "interface model.Flyer\n" + "class model.Plane\n" + "model.Plane -> model.Flyer\n"
                            + "class model.Seagull\n" + "model.Seagull -> model.Flyer\n" + "class model.Launcher\n"
                            + "model.Launcher -> model.Plane\n" + "model.Launcher -> model.Flyer\n"
                            + "class ui.FlyerApp\n" + "ui.FlyerApp -> model.Launcher\n" + "ui.FlyerApp -> model.Plane\n"
                            + "ui.FlyerApp -> model.Seagull\n" + "ui.FlyerApp -> model.Flyer\n" + "@enduml",
                    getContentsFromPath("./data/tempUMLangWriterAllImport.uml"));
        } catch (Exception e) {
            fail();
        }

        File file = new File("./data/tempUMLangWriterAllImport.uml");
        file.delete();
    }

    // EFFECTS: gets the file contents from the specified path and "" if the path could not be read
    private String getContentsFromPath(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        String fileContents;
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            fileContents = contentBuilder.toString().trim();
        } catch (IOException e) {
            return "";
        }
        return fileContents;
    }
}
