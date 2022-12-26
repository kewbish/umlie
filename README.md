# UMLie
Made in Java, September 2022 to December 2022.
Created by [Kewbish](https://kewbi.sh/).
Released under the [MIT License](./LICENSE).

A UML generator for Java projects.

![Project screenshot](./data/Project%20Screenshot.png)

## Background

When I was first going through class material for one of my courses last term, I felt that the
process of analyzing imports and package relationships was getting a bit
repetitive after the first few examples. I was curious if this work could be
automated. After some searching, I found that IntelliJ itself has
a [built-in feature](https://www.jetbrains.com/help/idea/class-diagram.html) to
do just that — but the version of IntelliJ with that feature is more than $200 a
year.

This application will have similar features as the
IntelliJ Class Diagram Generator, but be more limited in feature scope.

## Usage 

### Adding Multiple Java Inputs to the Diagram

To add multiple Java inputs to the diagram, click the 'Add Java file' in the
lower-left corner. Select a Java file in the file-picker popup (several have
been
provided in
`test/files` for convenience) and click 'OK'. The file will then be
added to the list panel on the left-hand side of the GUI.

This can be repeated an arbitrary amount of times.

### Removing Java Inputs from the Diagram

To remove Java inputs from the diagram, select the input and press
<kbd>Delete</kbd> (the keyboard key). The Java file will be deleted, and the
next time the diagram is generated, the diagram will not include the file.

(To remove Java inputs in bulk, press the 'Clear all files' button in the
lower-middle. This clears the entire diagram and resets program state.)

### Accessing Diagram 

The UML diagram is generated on
the right-hand side of the GUI. To access it, load some Java files (for
convenience, a UMLie save file is available in `data/testReaderAllFiles.
json`) and click one of the diagram types below the placeholder pane. The
related UML diagram type will be generated and displayed.

### Saving Application State

To save application state, click 'Save UMLie file' on the lower-left side.
Enter a filepath and file name in the file-picker, and click 'OK'. The
program state will be saved to JSON in the specified location.

### Loading Application State

To load application state, click 'Load UMLie file' on the lower-left side of
the GUI. Enter a filepath in the file-picker, and click 'OK' (for
convenience, see existing save files in `data/`). The files will be loaded,
and the program will be ready to generate diagrams - click one of the
diagram types on the lower-right side.

