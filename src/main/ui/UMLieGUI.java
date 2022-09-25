package ui;

import model.*;
import persistence.JsonReader;
import persistence.JsonWriter;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// GUI interface for UMLie
public class UMLieGUI {

    private static final Pattern PACKAGE_NAME_REGEX = Pattern.compile("src\\/(main\\/(java\\/)?)?(.*)\\/(.*).java");

    private ImportDiagramParser parser;

    private final UMLangGenerator generator;
    private final Set<String> takenShortNames;
    private DiagramType currentDiagramType;
    private DefaultListModel<String> listModel;
    private SaveImageButton saveAsImageBtn;
    private SaveUMLangButton saveUmlButton;

    // EFFECTS: creates a new instance of the UMLie GUI
    public UMLieGUI() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            System.out.println("Couldn't set look and feel, continuing...");
        }

        parser = new ImportDiagramParser();
        generator = new UMLangGenerator(parser);
        generator.setShowDependencies(false);
        takenShortNames = new HashSet<>();
        currentDiagramType = DiagramType.PACKAGE;

        JFrame frame = new JFrame();

        JPanel titlePanel = createTitleHeader();
        JPanel listPanel = createFilesPanel();
        JPanel saveDiagramPanel = createSaveDiagramPanel();
        JPanel umlWrapperPanel = createUMLangWrapperPanel(frame);

        frame.setPreferredSize(new Dimension(500, 500));
        JPanel overallPanel = createWrapperPanel(titlePanel, listPanel, umlWrapperPanel, saveDiagramPanel);
        frame.add(overallPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("UMLie");
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new EventLogWindowAdapter());
    }

    // EFFECTS: creates the files panel with the file list and the file action buttons
    private JPanel createFilesPanel() {
        JPanel listPanel = createFileListPanel();
        JPanel buttonPanel = createListControlPanel();
        listPanel.add(buttonPanel);
        return listPanel;
    }

    // EFFECTS: creates the wrapper panel around all components, initializes layout
    private static JPanel createWrapperPanel(JPanel titlePanel, JPanel listPanel, JPanel umlWrapperPanel,
                                             JPanel saveDiagramPanel) {
        JPanel mainFunctionalityPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainFunctionalityPanel);
        mainFunctionalityPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(saveDiagramPanel)
                        .addGroup(layout.createSequentialGroup().addComponent(listPanel).addComponent(umlWrapperPanel))
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(saveDiagramPanel)
                .addGroup(layout.createParallelGroup().addComponent(listPanel).addComponent(umlWrapperPanel))
        );

        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
        overallPanel.add(titlePanel);
        overallPanel.add(mainFunctionalityPanel);
        return overallPanel;
    }

    // EFFECTS: returns the save diagram panel with the 'save as image' and 'save as UML' buttons
    private JPanel createSaveDiagramPanel() {
        JPanel saveDiagramPanel = new JPanel();
        saveDiagramPanel.setLayout(new BoxLayout(saveDiagramPanel, BoxLayout.X_AXIS));
        JCheckBox checkBox = new JCheckBox("Show dependencies");
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                generator.setShowDependencies(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        SaveImageButton saveImageButton = new SaveImageButton(generator);
        this.saveAsImageBtn = saveImageButton;
        SaveUMLangButton saveUmlButton = new SaveUMLangButton(generator);
        this.saveUmlButton = saveUmlButton;
        saveDiagramPanel.add(checkBox);
        saveDiagramPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        saveDiagramPanel.add(saveUmlButton);
        saveDiagramPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        saveDiagramPanel.add(saveImageButton);
        return saveDiagramPanel;
    }

    // EFFECTS: creates the list control panel with the file action buttons
    private JPanel createListControlPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JButton addNewJavaInput = createAddNewJavaInputButton();
        buttonPanel.add(addNewJavaInput);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton loadFiles = createLoadUMLieFileButton();
        buttonPanel.add(loadFiles);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton saveFiles = createSaveUMLieFileButton();
        buttonPanel.add(saveFiles);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton clearFiles = createClearFilesButton();
        buttonPanel.add(clearFiles);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        return buttonPanel;
    }

    // EFFECTS: creates the Java input adder, which opens a file chooser and patches in a package description before
    // adding them to the parser
    private JButton createAddNewJavaInputButton() {
        JButton addNewJavaInput = new JButton("Add Java file");
        addNewJavaInput.addActionListener(
                new SaveTypeButtonActionListener(this::getResultAndAddJavaInput, "Java files", "java", false));
        return addNewJavaInput;
    }

    // EFFECTS: gets selected filepath from filechooser and validates Java file
    // then adds to the parser
    private void getResultAndAddJavaInput(JFileChooser fileChooser) {
        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();
        if (filePath.length() == 0 || takenShortNames.contains(filePath)) {
            JOptionPane.showMessageDialog(null,
                    filePath + " has been added already. Please choose another name.");
            return;
        }
        if (!filePath.endsWith(".java")) {
            JOptionPane.showMessageDialog(null, "This doesn't look like a Java file. Please rename your "
                    + "file to end in .java or try another Java file.");
            return;
        }
        createAndAddJavaInput(filePath);
    }

    // MODIFIES: this
    // EFFECTS: patches package information in and adds Java input to the parser
    private void createAndAddJavaInput(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        String fileContents;
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            fileContents = contentBuilder.toString().trim();
            if (fileContents.length() != 0) {
                // if no package declaration, try to create one from the pathname
                if (!fileContents.trim().startsWith("package")) {
                    Matcher matcher = PACKAGE_NAME_REGEX.matcher(filePath);
                    if (matcher.find() && matcher.group(2).length() == 0) {
                        String packageName = matcher.group(2).replace("\\/", ".");
                        fileContents = "package " + packageName + ";\n" + fileContents;
                    }
                }
                addJavaInputToGUI(filePath, fileContents);
            } else {
                throw new IOException();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "File was empty or could not be read. Try another file.");
        }
    }

    // MODIFIES: this
    // EFFECTS: adds the Java Input to the GUI
    private void addJavaInputToGUI(String filePath, String fileContents) {
        parser.addJavaInput(new JavaInput(filePath, fileContents));
        takenShortNames.add(filePath);
        listModel.addElement(filePath);
        saveAsImageBtn.setEnabled(true);
        saveUmlButton.setEnabled(true);
    }

    // EFFECTS: creates the UML load file button, which opens a file chooser and reads the UMLie save file in
    private JButton createLoadUMLieFileButton() {
        JButton loadFiles = new JButton("Load UMLie file");
        loadFiles.addActionListener(new SaveTypeButtonActionListener((JFileChooser fileChooser) -> {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            JsonReader jsonReader = new JsonReader(filePath);
            try {
                setUMLieStateFromJson(jsonReader);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error reading the file.");
            }
        }, "UMLie file", "json", true));
        return loadFiles;
    }

    // MODIFIES: this
    // EFFECTS: sets GUI state from loaded JSON file
    private void setUMLieStateFromJson(JsonReader jsonReader) throws IOException {
        parser = jsonReader.read();
        generator.setParser(parser);
        for (JavaInput javaInput : parser.getJavaInputs()) {
            listModel.addElement(javaInput.getShortName());
        }
        if (parser.getJavaInputs().size() != 0) {
            saveAsImageBtn.setEnabled(true);
            saveUmlButton.setEnabled(true);
        }
    }

    // EFFECTS: creates the UML file save button, which opens the file selector and writes UMLie state to JSON
    private JButton createSaveUMLieFileButton() {
        JButton saveFiles = new JButton("Save UMLie file");
        saveFiles.addActionListener(new SaveTypeButtonActionListener((JFileChooser fileChooser) -> {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            JsonWriter writer = new JsonWriter(filePath);
            try {
                writer.write(parser);
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Error reading the file.");
            }
        }, "UMLie files", "json", false));
        return saveFiles;
    }

    // EFFECTS: creates the file clear button, which resets program state
    private JButton createClearFilesButton() {
        JButton clearFiles = new JButton("Clear all files");
        clearFiles.addActionListener(e -> {
            parser.clearJavaInputs();
            generator.setParser(parser);
            listModel.clear();
            saveAsImageBtn.setEnabled(false);
            saveUmlButton.setEnabled(false);
        });
        return clearFiles;
    }

    // EFFECTS: creates the UML wrapper panel with the UML image display
    private JPanel createUMLangWrapperPanel(JFrame frame) {
        JPanel umlWrapperPanel = new JPanel();
        umlWrapperPanel.setLayout(new BoxLayout(umlWrapperPanel, BoxLayout.Y_AXIS));
        JPanel umlPanel = new JPanel();
        umlPanel.setBackground(new Color(0x424242));
        JLabel umlPlaceholder = new JLabel("Generate a diagram or add more files to get started.", JLabel.CENTER);
        umlPanel.setLayout(new BorderLayout());
        umlPanel.add(umlPlaceholder, BorderLayout.CENTER);
        JScrollPane umlScroller = new JScrollPane(umlPanel);
        umlScroller.setPreferredSize(new Dimension(frame.getWidth() / 2, 1000));
        umlWrapperPanel.add(umlScroller);

        JPanel umlControlPanel = createUMLangControlPanel(umlPanel);
        umlWrapperPanel.add(umlControlPanel);
        return umlWrapperPanel;
    }

    // EFFECTS: creates the UML control panel with the diagram type picker buttons
    private JPanel createUMLangControlPanel(JPanel umlPanel) {
        JPanel umlControlPanel = new JPanel();
        umlControlPanel.setLayout(new BoxLayout(umlControlPanel, BoxLayout.X_AXIS));
        JButton packageButton = new ImageGeneratorButton(DiagramType.PACKAGE, umlPanel,
                generator, () -> {
            setCurrentDiagramType(DiagramType.PACKAGE);
        }, saveAsImageBtn, saveUmlButton);
        umlControlPanel.add(packageButton);
        umlControlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton importsButton = new ImageGeneratorButton(DiagramType.CLASS_IMPORT, umlPanel,
                generator, () -> {
            setCurrentDiagramType(DiagramType.CLASS_IMPORT);
        }, saveAsImageBtn, saveUmlButton);
        umlControlPanel.add(importsButton);
        umlControlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton relationshipsButton = new ImageGeneratorButton(DiagramType.CLASS_RELATIONSHIP, umlPanel,
                generator, () -> {
            setCurrentDiagramType(DiagramType.CLASS_RELATIONSHIP);
        }, saveAsImageBtn, saveUmlButton);
        umlControlPanel.add(relationshipsButton);
        umlControlPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        return umlControlPanel;
    }


    // EFFECTS: returns the title header panel
    private JPanel createTitleHeader() {
        JLabel title = new JLabel("Welcome to UMLie!", JLabel.CENTER);
        Font newLabelFont = new Font(title.getFont().getName(), Font.BOLD, title.getFont().getSize());
        title.setFont(newLabelFont);
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(20, 20, 0, 20));
        titlePanel.add(title);
        return titlePanel;
    }

    // MODIFIES: this
    // EFFECTS: returns the file list panel
    private JPanel createFileListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        DefaultListModel listModel = new DefaultListModel();
        this.listModel = listModel;

        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.setVisibleRowCount(5);
        addDeleteKeybinding(listModel, list);

        JScrollPane listScrollPane = new JScrollPane(list);
        listPanel.add(listScrollPane);
        return listPanel;
    }

    // MODIFIES: listModel, list
    // EFFECTS: binds the delete key to delete the currently selected listModel element
    private void addDeleteKeybinding(DefaultListModel listModel, JList list) {
        InputMap inputMap = list.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteListSelection");
        ActionMap actionMap = list.getActionMap();
        actionMap.put("deleteListSelection", new DeleteListAbstractAction(listModel, list));
    }

    // wrapper action to delete selected items from the list of Java INputs
    private class DeleteListAbstractAction extends AbstractAction {
        private DefaultListModel listModel;
        private JList list;

        // EFFECTS: instantiates a new abstract action with the given list model
        public DeleteListAbstractAction(DefaultListModel listModel, JList list) {
            this.listModel = listModel;
            this.list = list;
        }

        // EFFECTS: removes the selected item from the list model
        // taken from https://stackoverflow.com/a/65487077
        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = listModel.getElementAt(list.getSelectedIndex()).toString();
            for (JavaInput javaInput : parser.getJavaInputs()) {
                if (javaInput.getShortName().equals(selected)) {
                    parser.removeJavaInput(javaInput);
                    break;
                }
            }
            if (parser.getJavaInputs().size() == 0) {
                saveAsImageBtn.setEnabled(false);
                saveUmlButton.setEnabled(false);
            }
            listModel.removeElementAt(list.getSelectedIndex());
            list.repaint();
        }
    }

    // setters

    public void setCurrentDiagramType(DiagramType currentDiagramType) {
        this.currentDiagramType = currentDiagramType;
    }
}
