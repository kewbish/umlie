package model;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

// collects all interfaces that the class in the currently parsed Java file implements
public class ImplementedTypesCollector extends VoidVisitorAdapter<List<String>> {
    // MODIFIES: collector
    // EFFECTS: adds the names of all implemented interfaces to the collector List
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        NodeList<ClassOrInterfaceType> implementDecls = n.getImplementedTypes();
        List<String> implementDeclStrings = new ArrayList<>();
        for (ClassOrInterfaceType decl : implementDecls) {
            implementDeclStrings.add(decl.getNameAsString());
        }
        collector.addAll(implementDeclStrings);
    }
}