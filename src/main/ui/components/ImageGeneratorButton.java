package ui.components;

import model.DiagramType;
import model.UMLangGenerator;

import javax.swing.*;

// creates an image generator button based on the diagram type
public class ImageGeneratorButton extends JButton {
    // MODIFIES: this, imageBtn, umlBtn
    // EFFECTS: instantiates a new JButton to generate the appropriate image type
    public ImageGeneratorButton(DiagramType diagramType, JPanel umlPanel, UMLangGenerator generator,
                                Runnable function, SaveTypeButton imageBtn, SaveTypeButton umlBtn) {
        super(diagramType == DiagramType.CLASS_IMPORT ? "Class imports" :
                (diagramType == DiagramType.CLASS_RELATIONSHIP ? "Class relationships" : "Package relationships"));
        addActionListener(
                new ImageGeneratorActionListener(umlPanel, diagramType, generator, function, imageBtn, umlBtn));
        imageBtn.setCurrentDiagramType(diagramType);
        umlBtn.setCurrentDiagramType(diagramType);
    }
}
