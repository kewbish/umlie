package model;

import java.util.Objects;

// wrapper class to associate association types and arities
public class AssociationRelation {
    private int arity;
    private final AssociationType type; // UMLIE_AGGREGATION

    // EFFECTS: creates a new association relationship with the given type and arity
    public AssociationRelation(int arity, AssociationType type) {
        this.arity = arity;
        this.type = type;
    }

    // getters
    public int getArity() {
        return arity;
    }

    public AssociationType getType() {
        return type;
    }

    // setters
    public void setArity(int arity) {
        this.arity = arity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssociationRelation relation = (AssociationRelation) o;

        if (arity != relation.arity) {
            return false;
        }
        return type == relation.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arity, type);
    }
}
