package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypeNameDeclarationTest {
    private TypeNameDeclaration typeNameDeclaration;

    @Test
    public void testTypeNameDeclarationConstructorNoInterface() {
        typeNameDeclaration = new TypeNameDeclaration("TestType");
        assertEquals("TestType", typeNameDeclaration.getName());
        assertFalse(typeNameDeclaration.isInterface());
    }

    @Test
    public void testTypeNameDeclarationConstructorWithFalseInterface() {
        typeNameDeclaration = new TypeNameDeclaration("TestType", false);
        assertEquals("TestType", typeNameDeclaration.getName());
        assertFalse(typeNameDeclaration.isInterface());
    }

    @Test
    public void testTypeNameDeclarationConstructorIsInterface() {
        typeNameDeclaration = new TypeNameDeclaration("TestType", true);
        assertEquals("TestType", typeNameDeclaration.getName());
        assertTrue(typeNameDeclaration.isInterface());
    }

    @Test
    public void testTypeNameDeclarationEquals() {
        typeNameDeclaration = new TypeNameDeclaration("TestType", true);
        TypeNameDeclaration typeNameDeclaration2 = new TypeNameDeclaration("TestType", true);
        TypeNameDeclaration typeNameDeclaration3 = new TypeNameDeclaration("Test", true);
        TypeNameDeclaration typeNameDeclaration4 = new TypeNameDeclaration("TestType", false);
        assertEquals(typeNameDeclaration, typeNameDeclaration2);
        assertEquals(typeNameDeclaration.hashCode(), typeNameDeclaration2.hashCode());
        assertFalse(typeNameDeclaration.equals(null));
        assertFalse(typeNameDeclaration.equals(3));
        assertEquals(typeNameDeclaration, typeNameDeclaration);
        assertNotEquals(typeNameDeclaration, typeNameDeclaration3);
        assertNotEquals(typeNameDeclaration, typeNameDeclaration4);
    }
}
