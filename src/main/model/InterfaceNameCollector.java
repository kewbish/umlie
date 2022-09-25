package model;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

// collects all interfaces defined in the currently parsed Java file
public class InterfaceNameCollector extends VoidVisitorAdapter<List<String>> {
    // MODIFIES: collector
    // EFFECTS: adds the names of all interfaces found to the collector List
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        if (n.isInterface()) {
            collector.add(n.getNameAsString());
        }
    }
}
