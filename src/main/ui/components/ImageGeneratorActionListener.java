package ui.components;

import model.DiagramType;
import model.UMLangGenerator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

// action listener to generate images from UML on button click
public class ImageGeneratorActionListener extends ImageGenerator implements ActionListener {
    private final JPanel umlPanel;
    private final Runnable function;
    private final DiagramType type; // UMLIE_AGGREGATION
    private final JButton imgBtn;
    private final JButton umlBtn;

    // EFFECTS: creates a new image generator listeners with associated JPanel, diagram type, and diagram generator
    public ImageGeneratorActionListener(JPanel umlPanel, DiagramType type, UMLangGenerator generator,
                                        Runnable function, JButton imgBtn, JButton umlBtn) {
        super(generator);
        this.umlPanel = umlPanel;
        this.type = type;
        this.function = function;
        this.imgBtn = imgBtn;
        this.umlBtn = umlBtn;
    }

    // MODIFIES: this
    // EFFECTS: sets the UML panel's contents to the displayed diagram or a placeholder message if no files are present
    @Override
    public void actionPerformed(ActionEvent e) {
        function.run();
        if (umlPanel.getComponentCount() > 0) {
            umlPanel.remove(0);
        }
        BufferedImage image = generateImage(type);
        if (image == null) {
            JLabel umlPlaceholder = new JLabel("Add more files to get started.", JLabel.CENTER);
            umlPanel.add(umlPlaceholder);
            imgBtn.setEnabled(false);
            umlBtn.setEnabled(false);
        } else {
            JLabel imageAsLabel = new JLabel(new ImageIcon(image));
            umlPanel.add(imageAsLabel);
        }
        umlPanel.revalidate();
    }
}
