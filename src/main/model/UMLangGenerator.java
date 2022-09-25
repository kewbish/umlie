package model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ImportDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// generates UML for package relationship and class import diagrams
public class UMLangGenerator {

    private ImportDiagramParser parser;
    private boolean showDependencies = true;

    // EFFECTS: initializes a new diagram generator with an ImportDiagramParser that shows dependencies in generated
    // output by default
    public UMLangGenerator(ImportDiagramParser parser) {
        this.parser = parser;
    }

    // MODIFIES: this
    // EFFECTS: generates the package import diagram for all Java inputs currently loaded in the parser
    // (package import diagram maps only the relationships between, and not inside, packages)
    public String generateUMLangPackageRelationshipDiagram() {
        String uml = "";
        Set<String> packages = new HashSet<>();
        Set<String> imports = new HashSet<>();

        generateInterPackageImportUML(packages, imports);

        for (String packageName : packages) {
            uml = uml.concat(packageName + "\n");
        }

        for (String importDecl : imports) {
            uml = uml.concat(importDecl + "\n");
        }

        EventLog.getInstance().logEvent(new Event("Package relationship diagram generated."));
        return wrapUml(uml);
    }

    // MODIFIES: this
    // EFFECTS: generates the class (intra-package) import diagram for all Java inputs currently loaded in the parser
    public String generateClassImportDiagram() {
        String uml = "";

        for (JavaInput javaInput : parser.getJavaInputs()) {
            String javaContents = javaInput.getJavaContents();
            parser.setParser(StaticJavaParser.parse(javaContents));

            TypeNameDeclaration typeName = parser.findTypeName();
            List<ImportDeclaration> explicitImports = parser.findAllExplicitImports();
            List<String> implicitImports = parser.findAllImplicitImports(javaInput);
            String finalPackageName = parser.findFinalTypeName();
            if (finalPackageName.trim().length() == 0) {
                continue;
            }

            uml = uml.concat((typeName.isInterface() ? "interface " : "class ") + finalPackageName + "\n");
            for (ImportDeclaration importDecl : explicitImports) {
                uml = uml.concat(finalPackageName + " -> " + importDecl.getNameAsString() + "\n");
            }
            for (String importDecl : implicitImports) {
                uml = uml.concat(finalPackageName + " -> " + importDecl + "\n");
            }
        }

        EventLog.getInstance().logEvent(new Event("Class import diagram generated."));

        return wrapUml(uml);
    }

    // MODIFIES: this
    // EFFECTS: generates the class (intra-package) import diagram for all Java inputs currently loaded
    public String generateClassRelationshipDiagram() {
        String uml = "";

        for (JavaInput javaInput : parser.getJavaInputs()) {
            parser.setCurrentJavaInput(javaInput);
            String javaContents = javaInput.getJavaContents();
            parser.setParser(StaticJavaParser.parse(javaContents));

            TypeNameDeclaration typeName = parser.findTypeName();
            List<ImportDeclaration> explicitImports = parser.findAllExplicitImports();
            List<String> implicitImports = parser.findAllImplicitImports(javaInput);
            String finalPackageName = getParser().findFinalTypeName();
            if (finalPackageName.trim().length() == 0) {
                continue;
            }

            uml = uml.concat((typeName.isInterface() ? "interface " : "class ") + finalPackageName + "\n");
            uml = uml.concat(generateImportsUML(explicitImports, implicitImports, finalPackageName));

            uml = uml.concat(generateAssociationRelationsUML());
            if (showDependencies) {
                uml = uml.concat(generateDependencyRelationsUML(uml));
            }
        }

        EventLog.getInstance().logEvent(new Event("Class relationship diagram generated."));
        return wrapUml(uml);
    }

    // MODIFIES: uml
    // EFFECTS: generates the imports and extends/implements UML relationships for the Java inputs
    private String generateImportsUML(List<ImportDeclaration> explicitImports, List<String> implicitImports,
                                      String finalPackageName) {
        String uml = "";
        uml = uml.concat(generateSuperTypesUML(finalPackageName, explicitImports, implicitImports,
                parser.findImplementedInterfaces(), true));
        uml = uml.concat(generateSuperTypesUML(finalPackageName, explicitImports, implicitImports,
                parser.findExtendedClasses(), false));
        return uml;
    }

    // MODIFIES: this
    // EFFECTS: generates the correct diagram for the provided diagram type
    public String generateDiagramForType(DiagramType type) {
        switch (type) {
            case CLASS_IMPORT:
                return generateClassImportDiagram();
            case CLASS_RELATIONSHIP:
                return generateClassRelationshipDiagram();
            default:
                return generateUMLangPackageRelationshipDiagram();
        }
    }

    // MODIFIES: this
    // EFFECTS: generates the UML association arrows for a map of association relationships
    private String generateAssociationRelationsUML() {
        String uml = "";
        Map<String, List<AssociationRelation>> associationRelations = parser.findAllAssociations();
        for (String key : associationRelations.keySet()) {
            List<AssociationRelation> relationships = associationRelations.get(key);
            for (AssociationRelation relationship : relationships) {
                if (relationship.getType() == AssociationType.DEPENDENCY) {
                    continue;
                }
                uml = uml.concat(parser.findFinalTypeName() + " " + (relationship.getType() == AssociationType.FIELD
                        || relationship.getType() == AssociationType.LIST_OF ? "-->" : "o-->") + " \"" + (
                        (relationship.getType() != AssociationType.AGGREGATE_LIST_OF
                                && relationship.getType() != AssociationType.LIST_OF) ? relationship.getArity() : "0."
                                + ".*")
                        + "\" " + key + "\n");
            }
        }
        return uml;
    }

    // MODIFIES: this
    // EFFECTS: generates the UML dependency arrows for a map of association relationships
    private String generateDependencyRelationsUML(String currentUml) {
        String uml = "";
        Map<String, List<AssociationRelation>> associationRelations = parser.findAllAssociations();
        for (String key : associationRelations.keySet()) {
            List<AssociationRelation> relationships = associationRelations.get(key);
            for (AssociationRelation relationship : relationships) {
                Pattern regex = Pattern.compile(
                        "(" + parser.findFinalTypeName().replace(".", "\\.") + " [-\\.|]+> " + key.replace(".", "\\.")
                                + "[^a-zA-Z\\d])");
                Matcher matcher = regex.matcher(currentUml);
                if (relationship.getType() == AssociationType.DEPENDENCY && !matcher.find()) {
                    uml = uml.concat(parser.findFinalTypeName() + " -.-> " + key + "\n");
                }
            }
        }
        return uml;
    }


    // MODIFIES: this, packages, imports
    // EFFECTS: appends the UML for the set of packages to the given packages Object, and appends the UML for the
    // package dependencies to the given imports Object
    private void generateInterPackageImportUML(Set<String> packages, Set<String> imports) {
        for (JavaInput javaInput : parser.getJavaInputs()) {
            String javaContents = javaInput.getJavaContents();
            parser.setParser(StaticJavaParser.parse(javaContents));

            String packageName = parser.findPackageName();
            List<ImportDeclaration> importDecls = parser.findAllExplicitImports();

            if (packageName.trim().length() != 0) {
                packages.add("folder " + packageName);
                for (ImportDeclaration importDecl : importDecls) {
                    String importDeclString = importDecl.getNameAsString();
                    if (!importDeclString.contains(packageName) && importDeclString.contains(".")
                            && !importDeclString.startsWith("java")
                    ) {
                        imports.add(packageName + " -> " + importDeclString.substring(0,
                                importDeclString.lastIndexOf('.')));
                    }
                }
            }

        }
    }

    // REQUIRES: all elements in superTypes are in at least one of explicitImports and implicitImports
    // EFFECTS: generates the UML representing class inheritance hierarchies for a class given its explicit /
    // implicit imports, supertypes, and its extension type (implementation or extension)
    private String generateSuperTypesUML(String finalPackageName, List<ImportDeclaration> explicitImports,
                                         List<String> implicitImports, List<String> superTypes, boolean isImplemented) {
        String uml = "";
        for (String implementedType : superTypes) {
            for (ImportDeclaration importDecl : explicitImports) {
                if (importDecl.getNameAsString().substring(importDecl.getNameAsString().lastIndexOf('.') + 1)
                        .equals(implementedType)) {
                    uml = uml.concat(finalPackageName + " " + (isImplemented ? "..|>" : "--|>") + " "
                            + importDecl.getNameAsString() + "\n");
                }
            }
            for (String importDecl : implicitImports) {
                if (importDecl.substring(importDecl.lastIndexOf('.') + 1).equals(implementedType)) {
                    uml = uml.concat(
                            finalPackageName + " " + (isImplemented ? "..|>" : "--|>") + " " + importDecl + "\n");
                }
            }
        }
        return uml;
    }

    // EFFECTS: wraps the provided string in "@startuml" and "@enduml" tags
    private String wrapUml(String uml) {
        return "@startuml\n" + uml + "@enduml";
    }

    // getters

    public ImportDiagramParser getParser() {
        return parser;
    }

    public boolean isShowingDependencies() {
        return showDependencies;
    }

    // setters

    public void setParser(ImportDiagramParser parser) {
        this.parser = parser;
    }

    public void setShowDependencies(boolean showDependencies) {
        this.showDependencies = showDependencies;
    }
}
