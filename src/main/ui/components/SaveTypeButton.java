package ui.components;

import model.DiagramType;
import model.UMLangGenerator;

import javax.swing.*;

// button to save the diagram as an image
public class SaveTypeButton extends JButton {
    protected UMLangGenerator generator;
    protected DiagramType currentDiagramType; // UMLIE_AGGREGATION

    // EFFECTS: creates a new JButton with the correct text
    public SaveTypeButton(UMLangGenerator generator, String text) {
        super(text);
        setEnabled(false);
        this.generator = generator;
    }

    // setters
    public void setCurrentDiagramType(DiagramType currentDiagramType) {
        this.currentDiagramType = currentDiagramType;
    }
}

