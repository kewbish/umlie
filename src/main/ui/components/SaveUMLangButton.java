package ui.components;

import model.UMLangGenerator;
import persistence.UMLangWriter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

// button to save the diagram as a UML file
public class SaveUMLangButton extends SaveTypeButton {
    // EFFECTS: creates a new JButton with 'Save as UML' text
    public SaveUMLangButton(UMLangGenerator generator) {
        super(generator, "Save as UML");
        addActionListener(new SaveTypeButtonActionListener((JFileChooser fileChooser) -> {
            saveUML(fileChooser);
        }, "UML files", "uml", false));
    }

    // EFFECTS: saves the UML to the file
    private void saveUML(JFileChooser fileChooser) {
        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();
        UMLangWriter writer = new UMLangWriter(filePath, generator);
        try {
            writer.writeUML(currentDiagramType);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't write to file, try again.");
        }
    }
}
