package model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writeable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// parses Java files and finds class / import declarations and extends / implements relationships
public class ImportDiagramParser implements Writeable {

    private CompilationUnit parser;

    private List<JavaInput> javaInputs; // UMLIE_AGGREGATION

    private JavaInput currentJavaInput; // UMLIE_AGGREGATION

    // EFFECTS: initializes a Java parser with no Java files added
    // https://github.com/javaparser/javasymbolsolver-maven-sample
    public ImportDiagramParser() {
        javaInputs = new ArrayList<>();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }

    // EFFECTS: initializes a Java parser with a single string representing a Java file
    public ImportDiagramParser(JavaInput javaInput) {
        this();
        addJavaInput(javaInput);
        currentJavaInput = javaInput;
    }

    // EFFECTS: initializes a Java parser with multiple strings representing Java files
    public ImportDiagramParser(List<JavaInput> javaInputs) {
        this();
        for (JavaInput javaInput : javaInputs) {
            addJavaInput(javaInput);
        }
        if (this.javaInputs.size() > 0) {
            currentJavaInput = this.javaInputs.get(0);
        }
    }

    // EFFECTS: returns the package name in the Java file currently being parsed
    public String findPackageName() {
        String packageName = "";
        List<PackageDeclaration> packageNames = parser.findAll(PackageDeclaration.class);
        if (packageNames.size() > 0) {
            packageName = packageNames.get(0).getNameAsString();
        }
//        EventLog.getInstance().logEvent(new Event("Package name " + packageName + " found."));
        return packageName;
    }

    // REQUIRES: only one class declaration in the file
    // EFFECTS: returns the class / interface name in the file currently being parsed (assumes there is only one; if
    // there are more than one return the first class declaration encountered)
    public TypeNameDeclaration findTypeName() {
        List<String> classNames = new ArrayList<>();
        ClassNameCollector classNameVisitor = new ClassNameCollector();
        classNameVisitor.visit(parser, classNames);

        List<String> interfaceNames = new ArrayList<>();
        InterfaceNameCollector interfaceNameCollector = new InterfaceNameCollector();
        interfaceNameCollector.visit(parser, interfaceNames);

        String typeName = null;
        if (classNames.size() > 0) {
            typeName = classNames.get(0);
        } else if (interfaceNames.size() > 0) {
            typeName = interfaceNames.get(0);
        }
//        EventLog.getInstance().logEvent(new Event("Type name " + typeName + " found."));
        return new TypeNameDeclaration(typeName, interfaceNames.contains(typeName));
    }

    // EFFECTS: returns the full type name ("[package name].[class/interface name]")
    public String findFinalTypeName() {
        String packageName = findPackageName();
        String className = findTypeName().getName();
        if (className == null) {
            return "";
        }
        String finalName = (packageName.trim().length() != 0 ? packageName + "." : "") + className;
//        EventLog.getInstance().logEvent(new Event("Final type name " + finalName + " found."));
        return finalName;
    }

    // REQUIRES: only one class declaration in the file
    // EFFECTS: returns all interfaces the current class implements
    public List<String> findImplementedInterfaces() {
        return findSuperTypes(new ImplementedTypesCollector());
    }

    // REQUIRES: only one class declaration in the file
    // EFFECTS: returns all superclasses the current class extends
    public List<String> findExtendedClasses() {
        return findSuperTypes(new ExtendedTypesCollector());
    }

    // EFFECTS: visits all nodes and returns all interfaces or superclasses that this class is a subclass of
    private List<String> findSuperTypes(VoidVisitorAdapter<List<String>> collector) {
        List<String> typeNames = new ArrayList<>();
        collector.visit(parser, typeNames);
        return typeNames;
    }

    // TODO - resolve wildcard imports
    // REQUIRES: no wildcard imports in currently parsed Java file
    // EFFECTS: returns all explicit imports in the Java code that aren't from Java STDLIB
    public List<ImportDeclaration> findAllExplicitImports() {
        List<String> externalDefaultImports = Arrays.asList("java", "com", "ca", "org", "net");
        List<ImportDeclaration> imports = parser.findAll(ImportDeclaration.class);
        List<ImportDeclaration> sanitizedImports = new ArrayList<>();
        for (ImportDeclaration importDecl : imports) {
            if (!importDecl.getNameAsString().contains(".") || !externalDefaultImports.contains(
                    importDecl.getNameAsString().substring(0, importDecl.getNameAsString().indexOf(".")))) {
                sanitizedImports.add(importDecl);
            }
        }
//        EventLog.getInstance().logEvent(new Event("Explicit imports " + sanitizedImports + " found."));
        return sanitizedImports;
    }

    // REQUIRES: none of the current javaInputs have wildcard imports
    // MODIFIES: this
    // EFFECTS: finds all implicit imports in fromFile from the files currently added to the diagram
    // heuristic: if there are occurrences of a class name in fromFile and fromFile doesn't import any other classes
    // that could provide this name, it should come from fromFile
    public List<String> findAllImplicitImports(JavaInput fromFile) {
        this.currentJavaInput = fromFile;
        Set<String> typeNames = findAllTypeNamesInInputs();

        setParser(StaticJavaParser.parse(fromFile.getJavaContents()));
        List<ImportDeclaration> explicitImports = findAllExplicitImports();
        String currentTypeName = findTypeName().getName();
        String currentPackageName = findPackageName();

        Set<String> explicitImportStrings = new HashSet<>();
        for (ImportDeclaration explicitImport : explicitImports) {
            explicitImportStrings.add(explicitImport.getName().getIdentifier());
        }

        Set<String> implicitImports = new HashSet<>();
        for (String typeName : typeNames) {
            Pattern regex = Pattern.compile("([^a-zA-Z\\d]" + typeName + "[^a-zA-Z\\d])");
            Matcher matcher = regex.matcher(fromFile.getJavaContents());
            if (!explicitImportStrings.contains(typeName) && !typeName.equals(currentTypeName) && matcher.find()) {
                implicitImports.add(
                        (currentPackageName.trim().length() != 0 ? currentPackageName + "." : "") + typeName);
            }
        }
//        EventLog.getInstance().logEvent(new Event("Explicit imports " + implicitImports + " found."));
        return new ArrayList<>(implicitImports);
    }

    // MODIFIES: this
    // EFFECTS: creates a mapping of imported TypeName to their associations in the current class
    public Map<String, List<AssociationRelation>> findAllAssociations() {
        Set<String> typeNames = findAllFullTypeNamesInInputs();
        List<FieldDeclaration> fields = findAllFields();
        Map<String, List<AssociationRelation>> map = new HashMap<>();
        mapToAssociations(typeNames, fields, map);
//        EventLog.getInstance().logEvent(new Event("All JavaInputs have been mapped to their associations."));
        System.out.println(findFinalTypeName());
        System.out.println(map);
        return map;
    }

    // MODIFIES: typeNames, fields, map
    // EFFECTS: associates each class mentioned in a field in the current class to an arity and association type
    // dependency heuristic:  if it's mentioned in the file and is not a field, it's a dependency
    private void mapToAssociations(Set<String> typeNames, List<FieldDeclaration> fields,
                                   Map<String, List<AssociationRelation>> map) {
        String currentTypeName = findTypeName().getName();
        for (String typeName : typeNames) {
            for (FieldDeclaration field : fields) {
                if (typeName.contains(currentTypeName) && !field.isStatic()) {
                    continue;
                }
                if (!field.isStatic()) {
                    createAssociationRelationshipsForFields(fields, map, typeName, field);
                }
            }
            if (currentJavaInput.getJavaContents().contains(typeName) && !map.containsKey(typeName)) {
                AssociationRelation ar = new AssociationRelation(-1, AssociationType.DEPENDENCY);
                List<AssociationRelation> dependencyARList = new ArrayList<>();
                dependencyARList.add(ar);
                map.put(typeName, dependencyARList);
            }
        }
    }

    // MODIFIES: map
    // EFFECTS: creates association relation objects for every field relationship, returns true if fields were added
    private void createAssociationRelationshipsForFields(List<FieldDeclaration> fields,
                                                         Map<String, List<AssociationRelation>> map, String typeName,
                                                         FieldDeclaration field) {
        String rootTypeName = typeName.contains(".") ? typeName.substring(typeName.lastIndexOf('.') + 1) : typeName;
        boolean isAggregation = field.getComment().toString().contains("UMLIE_AGGREGATION");
        boolean listOf = isListOf(field, rootTypeName);
        int fieldCount = getFieldCount(fields, rootTypeName, listOf);
        if (fieldCount == 0) {
            return;
        }
        AssociationRelation ar = new AssociationRelation(fieldCount,
                listOf ? (isAggregation ? AssociationType.AGGREGATE_LIST_OF : AssociationType.LIST_OF)
                        : (isAggregation ? AssociationType.AGGREGATE_FIELD : AssociationType.FIELD));
        if (map.containsKey(typeName)) {
            if (listOf || !map.get(typeName).contains(ar)) {
                map.get(typeName).add(ar);
            }
        } else {
            List<AssociationRelation> newARList = new ArrayList<>();
            newARList.add(ar);
            map.put(typeName, newARList);
        }
    }

    // EFFECTS: gets the amount of non-static fields for a specific association
    private static int getFieldCount(List<FieldDeclaration> fields, String rootTypeName, boolean isListOf) {
        int fieldCount = (int) fields.stream()
                .filter(fieldDeclaration -> (isListOf ? (fieldDeclaration.getCommonType().asString()
                        .contains("<" + rootTypeName + ">") || (fieldDeclaration.getCommonType().asString()
                        .contains("<" + rootTypeName + ",")) || (fieldDeclaration.getCommonType().asString()
                        .contains("," + rootTypeName + ">")))
                        : fieldDeclaration.getCommonType().asString().equals(rootTypeName))
                ).count();
        return fieldCount;
    }

    // EFFECTS: returns true if the field is of type (Array)List, (Hash)Map, or (Hash)Set
    private boolean isListOf(FieldDeclaration field, String typeName) {
        return (field.getCommonType().asString().contains("List<" + typeName + ">")) || (field.getCommonType()
                .asString().contains("Set<" + typeName + ">")) || ((field.getCommonType().asString().contains("Map<")
                && (field.getCommonType().asString().contains("<" + typeName + ",") || field.getCommonType().asString()
                .contains("," + typeName + ">"))));
    }

    // REQUIRES: this.currentJavaInput is set
    // MODIFIES: this
    // EFFECTS: finds all the types declared in all javaInputs
    private Set<String> findAllTypeNamesInInputs() {
        Set<String> typeNames = new HashSet<>();
        for (JavaInput javaInput : javaInputs) {
            setParser(StaticJavaParser.parse(javaInput.getJavaContents()));
            String typeName = findTypeName().getName();
            if (typeName != null) {
                typeNames.add(typeName);
            }
        }
        setParser(StaticJavaParser.parse(currentJavaInput.getJavaContents()));
        return typeNames;
    }

    // REQUIRES: this.currentJavaInput is set
    // MODIFIES: this
    // EFFECTS: finds all the types declared in all javaInputs, with their fully quantified package names
    private Set<String> findAllFullTypeNamesInInputs() {
        Set<String> typeNames = new HashSet<>();
        for (JavaInput javaInput : javaInputs) {
            setParser(StaticJavaParser.parse(javaInput.getJavaContents()));
            String typeName = findFinalTypeName();
            typeNames.add(typeName);
        }
        setParser(StaticJavaParser.parse(currentJavaInput.getJavaContents()));
        return typeNames;
    }

    // EFFECTS: finds all fields / associations declared in a class
    private List<FieldDeclaration> findAllFields() {
        List<FieldDeclaration> fields = new ArrayList<>();
        for (TypeDeclaration typeDec : parser.getTypes()) {
            List<BodyDeclaration> members = typeDec.getMembers();
            for (BodyDeclaration member : members) {
                if (member instanceof FieldDeclaration) {
                    FieldDeclaration field = (FieldDeclaration) member;
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    // MODIFIES: this
    // EFFECTS: adds a Java file to the project being parsed and logs to the event log
    public void addJavaInput(JavaInput javaInput) {
        javaInputs.add(javaInput);
        EventLog.getInstance().logEvent(new Event("JavaInput with name " + javaInput.getShortName() + " added."));
    }

    // MODIFIES: this
    // EFFECTS: removes the given Java input from the list of inputs if it exists in the list already
    public void removeJavaInput(JavaInput javaInput) {
        javaInputs.remove(javaInput);
        EventLog.getInstance().logEvent(new Event("JavaInput with name " + javaInput.getShortName() + " removed."));
    }

    // MODIFIES: this
    // EFFECTS: clears all Java inputs
    public void clearJavaInputs() {
        javaInputs = new ArrayList<>();
        EventLog.getInstance().logEvent(new Event("JavaInputs cleared."));
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("javaInputs", javaInputsToJson());
        return json;
    }

    // EFFECTS: returns a JSONArray containing all Java Inputs
    private JSONArray javaInputsToJson() {
        JSONArray jsonArray = new JSONArray();

        for (JavaInput javaInput : javaInputs) {
            jsonArray.put(javaInput.toJson());
        }

        return jsonArray;
    }

    // getters

    public List<JavaInput> getJavaInputs() {
        return javaInputs;
    }

    public JavaInput getCurrentJavaInput() {
        return currentJavaInput;
    }

    // setters

    public void setParser(CompilationUnit parser) {
        this.parser = parser;
    }

    public void setCurrentJavaInput(JavaInput javaInput) {
        this.currentJavaInput = javaInput;
    }

}
