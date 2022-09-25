package ui.components;

import model.DiagramType;
import model.UMLangGenerator;
import net.sourceforge.plantuml.SourceStringReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// generates images from parser state
public class ImageGenerator {
    private final UMLangGenerator generator;

    public ImageGenerator(UMLangGenerator generator) {
        this.generator = generator;
    }

    // MODIFIES: this
    // EFFECTS: generates image from current generator UML
    public BufferedImage generateImage(DiagramType type) {
        // taken from https://plantuml.com/api
        String source = generator.generateDiagramForType(type);
        if (source.replace("@startuml", "").replace("@enduml", "")
                .replace("\n", "").trim().length() == 0) {
            return null;
        }
        ByteArrayOutputStream currentDiagram = new ByteArrayOutputStream();
        SourceStringReader reader = new SourceStringReader(source);
        try {
            reader.outputImage(currentDiagram);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        InputStream inputStream = new ByteArrayInputStream(currentDiagram.toByteArray());
        try {
            return ImageIO.read(inputStream);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't render UML, try again.");
        }
        return null;
    }
}
