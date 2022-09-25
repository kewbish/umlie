package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AssociationRelationTest {

    @Test
    public void testAssociationRelationConstructor() {
        AssociationRelation ar = new AssociationRelation(2, AssociationType.FIELD);
        assertEquals(2, ar.getArity());
        assertEquals(AssociationType.FIELD, ar.getType());
    }

    @Test
    public void testAssociationRelationAritySetter() {
        AssociationRelation ar = new AssociationRelation(1, AssociationType.FIELD);
        ar.setArity(2);
        assertEquals(2, ar.getArity());
    }

    @Test
    public void testAssociationRelationshipEquals() {
        AssociationRelation ar = new AssociationRelation(2, AssociationType.FIELD);
        AssociationRelation ar2 = new AssociationRelation(2, AssociationType.FIELD);
        AssociationRelation ar3 = new AssociationRelation(3, AssociationType.FIELD);
        AssociationRelation ar4 = new AssociationRelation(3, AssociationType.LIST_OF);
        assertEquals(ar, ar2);
        assertEquals(ar, ar);
        assertFalse(ar.equals(null));
        assertFalse(ar.equals(2));
        assertNotEquals(ar, ar3);
        assertNotEquals(ar, ar4);
        assertEquals(ar.hashCode(), ar2.hashCode());
    }
}
