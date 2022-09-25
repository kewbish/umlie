package model;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

// collects all class names defined in the currently parsed Java file
// from https://stackoverflow.com/a/65733145
public class ClassNameCollector extends VoidVisitorAdapter<List<String>> {
    // MODIFIES: collector
    // EFFECTS: adds all class names found to the collector List
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        if (!n.isInterface()) {
            collector.add(n.getNameAsString());
        }
    }
}
