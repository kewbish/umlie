package ui.components;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

// action listener class to run the appropriate function for 'save as type' buttons
public class SaveTypeButtonActionListener implements ActionListener {
    private Consumer<JFileChooser> function;
    private String description;
    private String extensions;
    private boolean loadInstead;

    // EFFECTS: creates a new ActionListener that associates the subfunction to run with the action listener
    public SaveTypeButtonActionListener(Consumer<JFileChooser> function, String description, String extensions,
                                        boolean loadInstead) {
        this.function = function;
        this.description = description;
        this.extensions = extensions;
        this.loadInstead = loadInstead;
    }

    // EFFECTS: displays a file chooser window and runs the consumer function on the selected file
    @Override
    public void actionPerformed(ActionEvent e) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
        JFileChooser fileChooser = new JFileChooser("");
        fileChooser.setFileFilter(filter);
        int result = loadInstead ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            function.accept(fileChooser);
        }
    }
}
