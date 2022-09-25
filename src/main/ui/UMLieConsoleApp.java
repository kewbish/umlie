package ui;

import model.DiagramType;
import model.ImportDiagramParser;
import model.JavaInput;
import model.UMLangGenerator;
import persistence.JsonReader;
import persistence.JsonWriter;
import persistence.UMLangWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// collects user input and dispatches method calls to the parser and diagram generator
// input loop adapted from TellerApp example - https://github.students.cs.ubc.ca/CPSC210/TellerApp
public class UMLieConsoleApp {

    private static final Pattern PACKAGE_NAME_REGEX = Pattern.compile("src\\/(main\\/(java\\/)?)?(.*)\\/(.*).java");

    private ImportDiagramParser parser;
    private final UMLangGenerator generator;
    // private Map<String, String> fileContents;
    private final Scanner input;
    private final Set<String> takenShortNames;

    // EFFECTS: instantiates a new instance of the app and runs the input loop
    public UMLieConsoleApp() {
        parser = new ImportDiagramParser();
        generator = new UMLangGenerator(parser);
        // fileContents = new HashMap<>();
        input = new Scanner(System.in);
        input.useDelimiter("\n");
        takenShortNames = new HashSet<>();
        runInputLoop();
    }

    // MODIFIES: this
    // EFFECTS: welcomes the user and infinitely loops while asking for a command to perform
    private void runInputLoop() {
        boolean keepGoing = true;
        String command;

        System.out.println("Welcome to UMLie - a UML diagram generator for Java projects.");

        while (keepGoing) {
            printOptions();
            command = input.next();
            command = command.toLowerCase();

            if (command.equals("q")) {
                keepGoing = false;
                System.out.println("Save before quitting? (y/n)");
                command = input.next().toLowerCase();
                if (command.equals("y")) {
                    saveJson();
                }
            } else {
                processCommand(command);
            }
        }

        System.out.println("\nThanks for using UMLie!");
    }

    // MODIFIES: this
    // EFFECTS: executes the appropriate function based on the command
    private void processCommand(String command) {
        System.out.println();
        switch (command) {
            case "new":
                resetState();
                break;
            case "addfile":
            case "add": {
                addFile();
                break;
            }
            case "removefile":
            case "remove": {
                removeFile();
                break;
            }
            case "list": {
                printFileSummaries();
                break;
            }
            default:
                processOtherCommandsI(command);
        }
        System.out.println();
    }

    // MODIFIES: this
    // EFFECTS: executes the appropriate function based on the command, split into two functions for line-length
    // requirements
    private void processOtherCommandsI(String command) {
        switch (command) {
            case "savejson":
            case "save": {
                saveJson();
                break;
            }
            case "loadjson":
            case "load": {
                readJson();
                break;
            }
            case "package": {
                System.out.println(generator.generateUMLangPackageRelationshipDiagram());
                break;
            }
            case "imports": {
                System.out.println(generator.generateClassImportDiagram());
                break;
            }
            default:
                processOtherCommandsII(command);
        }

    }

    // EFFECTS: executes the appropriate function based on the command, split into two functions for line-length
    // requirements
    private void processOtherCommandsII(String command) {
        switch (command) {
            case "relationship": {
                System.out.println(generator.generateClassRelationshipDiagram());
                break;
            }
            case "export": {
                try {
                    exportDiagram();
                } catch (IOException e) {
                    System.out.println("Error saving the file, please try again with a different filepath.");
                }
                break;
            }
            default:
                System.out.println("Invalid command.");
        }
    }

    // MODIFIES: this
    // EFFECTS: reads JSON and replaces the current diagram parser with the loaded parser
    private void readJson() {
        System.out.println("Enter a filepath:");
        String filePath = input.next();
        JsonReader jsonReader = new JsonReader(filePath);
        try {
            parser = jsonReader.read();
        } catch (IOException e) {
            System.out.println("Error reading the file.");
        }
        generator.setParser(parser);
    }

    // EFFECTS: exports the current diagram to a filepath on disk
    private void exportDiagram() throws IOException {
        System.out.println("Enter a filepath:");
        String filePath = input.next();
        System.out.println("Enter a diagram type (package, imports, relationship):");
        String diagramType = input.next();
        UMLangWriter writer = new UMLangWriter(filePath, generator);
        try {
            writer.writeUML(DiagramType.valueOf(diagramType));
        } catch (IllegalArgumentException e) {
            System.out.println("That diagram type doesn't exist.");
        }
    }

    // EFFECTS: prints a list of the type names defined in each input
    private void printFileSummaries() {
        List<JavaInput> javaInputs = this.parser.getJavaInputs();
        if (javaInputs.size() == 0) {
            System.out.println("No files added.");
            return;
        }
        int i = 0;
        for (JavaInput javaInput : javaInputs) {
            System.out.println("(" + (i + 1) + ") - " + javaInput.getShortName());
            i += 1;
        }
    }

    // MODIFIES: this
    // EFFECTS: prints filepath of each file added and asks the user which they would like to remove from the parser
    private void removeFile() {
        List<JavaInput> javaInputs = this.parser.getJavaInputs();
        if (this.parser.getJavaInputs().size() == 0) {
            System.out.println("No files have been added.");
            return;
        }
        System.out.println("Choose a file to remove:");
        printFileSummaries();
        int choice = input.nextInt();
        if (!(0 < choice && choice <= javaInputs.size())) {
            System.out.println("Choice out of range. Try again.");
            return;
        }
        choice -= 1;
        JavaInput chosenInput = javaInputs.get(choice);
        parser.removeJavaInput(chosenInput);
        takenShortNames.remove(chosenInput.getShortName());
    }

    // MODIFIES: this
    // EFFECTS: asks the user for a filepath to add to the diagram, checks if the input ends in .java
    private void addFile() {
        System.out.println("Enter a filepath:");
        String filePath = input.next();
        if (filePath.length() == 0 || this.takenShortNames.contains(filePath)) {
            System.out.println(filePath + " has been added already. Please choose another name.");
            return;
        }
        if (!filePath.endsWith(".java")) {
            System.out.println("This doesn't look like a Java file. Please rename your file to end in .java or try "
                    + "another Java file.");
            return;
        }
        addFileToParser(filePath);
    }

    // REQUIRES: filePath is a well-formed filepath
    // MODIFIES: this
    // EFFECTS: patches on a package declaration if one doesn't exist and adds the file to the parser
    private void addFileToParser(String filePath) {
        // adapted from https://howtodoinjava.com/java/io/java-read-file-to-string-examples/#2-using-fileslines-java-8
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
                this.parser.addJavaInput(new JavaInput(filePath, fileContents));
                this.takenShortNames.add(filePath);
            } else {
                System.out.println("File was empty or could not be read. Try another file.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // MODIFIES: this
    // EFFECTS: resets the state of the app and instantiates new objects
    private void resetState() {
        System.out.println("Clearing previous files...");
        parser = new ImportDiagramParser();
        generator.setParser(parser);
    }

    // EFFECTS: prints a command menu
    private void printOptions() {
        System.out.println("Commands:");
        System.out.println("- new to reset and create a new diagram");
        System.out.println("- addfile to add a file to your current diagram");
        System.out.println("- package to generate a package relationship diagram");
        System.out.println("- imports to generate an inter-class import relationship diagram");
        System.out.println("- relationship to generate an inter-class type extension diagram");
        System.out.println("- export to save a diagram as a UML file");
        System.out.println("- removefile to remove a file from your current diagram");
        System.out.println("- savejson to save your current diagram inputs to JSON");
        System.out.println("- loadjson to load a JSON state file into a diagram");
        System.out.println("- list to list current files");
        System.out.println("- q to quit");
    }

    // EFFECTS: saves the current diagram parser to JSON
    private void saveJson() {
        System.out.println("Enter a filepath:");
        String filePath = input.next();
        JsonWriter writer = new JsonWriter(filePath);
        try {
            writer.write(parser);
        } catch (FileNotFoundException e) {
            System.out.println("Error saving to " + filePath);
            return;
        }
        System.out.println("Saved to " + filePath + "!");
    }
}
