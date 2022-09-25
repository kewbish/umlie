package model;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

// collects all superclasses that the class in the currently parsed Java file extends
public class ExtendedTypesCollector extends VoidVisitorAdapter<List<String>> {
    // MODIFIES: collector
    // EFFECTS: adds all names of the extended superclasses to the collector List
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        NodeList<ClassOrInterfaceType> extendedDecls = n.getExtendedTypes();
        List<String> extendedDeclsStrings = new ArrayList<>();
        for (ClassOrInterfaceType decl : extendedDecls) {
            extendedDeclsStrings.add(decl.getNameAsString());
        }
        collector.addAll(extendedDeclsStrings);
    }
}
