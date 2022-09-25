package model;

import java.util.Objects;

// wrapper, keeps track of a type definition and whether it was declared as an interface or class
public class TypeNameDeclaration {
    private final String name;
    private final boolean isInterface;

    // EFFECTS: constructs a type name declaration with the given name, defaulting isInterface to false
    public TypeNameDeclaration(String name) {
        this.name = name;
        this.isInterface = false;
    }

    // EFFECTS: constructs a type name declaration with the given name and interface type
    public TypeNameDeclaration(String name, boolean isInterface) {
        this.name = name;
        this.isInterface = isInterface;
    }

    // getters

    public String getName() {
        return name;
    }

    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeNameDeclaration that = (TypeNameDeclaration) o;

        if (isInterface != that.isInterface) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isInterface);
    }
}
