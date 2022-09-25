package ui.components;

import model.DiagramType;
import model.UMLangGenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// button to save the diagram as an image
public class SaveImageButton extends SaveTypeButton {
    // EFFECTS: creates a new JButton with 'Save as Image' text
    public SaveImageButton(UMLangGenerator generator) {
        super(generator, "Save as Image");
        addActionListener(new SaveTypeButtonActionListener((JFileChooser fileChooser) -> {
            saveImage(fileChooser);
        }, "PNG images", "png", false));
    }

    // EFFECTS: saves the image to the file
    private void saveImage(JFileChooser fileChooser) {
        ImageGenerator imageGen = new ImageGenerator(generator);
        try {
            BufferedImage image = imageGen.generateImage(currentDiagramType);
            File selectedFile = fileChooser.getSelectedFile();
            ImageIO.write(image, "png", selectedFile);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't write to file, try again.");
        }
    }

    // setters
    public void setCurrentDiagramType(DiagramType currentDiagramType) {
        this.currentDiagramType = currentDiagramType;
    }
}

