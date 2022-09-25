package model;

import com.github.javaparser.StaticJavaParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static model.DummyJavaProject.*;
import static org.junit.jupiter.api.Assertions.*;

public class ImportDiagramParserTest {
    private ImportDiagramParser parser;

    @BeforeEach
    public void clearEventLog() {
        parser = new ImportDiagramParser();
        EventLog.getInstance().clear();
    }

    @Test
    public void testIDPConstructorNoFiles() {
        parser = new ImportDiagramParser();
        assertEquals(0, parser.getJavaInputs().size());
        assertNull(parser.getCurrentJavaInput());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            events.next();
        }
        assertEquals(0, eventSize);
    }

    @Test
    public void testIDPConstructorOneFile() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        assertEquals(1, parser.getJavaInputs().size());
        assertEquals(FLYER_JAVA, parser.getCurrentJavaInput());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            Event e = events.next();
            if (eventSize == 1) {
                assertTrue(e.getDescription().contains("JavaInput with name flyer-interface added."));
            }
        }
        assertEquals(1, eventSize);
    }

    @Test
    public void testIDPConstructorMultipleFiles() {
        parser = new ImportDiagramParser(javaInputs);
        assertEquals(5, parser.getJavaInputs().size());
        assertEquals(javaInputs.get(0), parser.getCurrentJavaInput());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            Event e = events.next();
            if (eventSize >= 1) {
                assertTrue(
                        e.getDescription().contains(
                                "JavaInput with name " + javaInputs.get(eventSize - 1).getShortName() + " added"
                                        + "."));
            }
        }
        assertEquals(5, eventSize);
    }

    @Test
    public void testIDPConstructorNoFilesArrayList() {
        parser = new ImportDiagramParser(new ArrayList<>());
        assertEquals(0, parser.getJavaInputs().size());
        assertNull(parser.getCurrentJavaInput());
    }

    @Test
    public void testIDPFindPackageName() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.setParser(StaticJavaParser.parse(FLYER_JAVA.getJavaContents()));
        assertEquals("model", parser.findPackageName());
        parser = new ImportDiagramParser(FLYER_APP_JAVA);
        parser.setParser(StaticJavaParser.parse(FLYER_APP_JAVA.getJavaContents()));
        assertEquals("ui", parser.findPackageName());
    }

    @Test
    public void testIDPFindTypeName() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.setParser(StaticJavaParser.parse(FLYER_JAVA.getJavaContents()));
        assertEquals("Flyer", parser.findTypeName().getName());
        assertTrue(parser.findTypeName().isInterface());
        parser = new ImportDiagramParser(PLANE_JAVA);
        parser.setParser(StaticJavaParser.parse(PLANE_JAVA.getJavaContents()));
        assertEquals("Plane", parser.findTypeName().getName());
        assertFalse(parser.findTypeName().isInterface());
    }

    @Test
    public void testIDPFindFinalTypeName() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.setParser(StaticJavaParser.parse(FLYER_JAVA.getJavaContents()));
        assertEquals("model.Flyer", parser.findFinalTypeName());
    }

    @Test
    public void testIDPFindFinalTypeNameNoDeclaration() {
        JavaInput testJava = new JavaInput("no-decl", "package model;");
        parser = new ImportDiagramParser(testJava);
        parser.setParser(StaticJavaParser.parse(testJava.getJavaContents()));
        assertEquals("", parser.findFinalTypeName());
    }

    @Test
    public void testIDPFindFinalTypeNameNoPackage() {
        JavaInput testJava = new JavaInput("no-package-bird", "class Bird {}");
        parser = new ImportDiagramParser(testJava);
        parser.setParser(StaticJavaParser.parse(testJava.getJavaContents()));
        assertEquals("Bird", parser.findFinalTypeName());
    }

    @Test
    public void testIDPFindImplementedInterfaces() {
        List<String> expected = new ArrayList<>();
        expected.add("Flyer");
        parser = new ImportDiagramParser(PLANE_JAVA);
        parser.setParser(StaticJavaParser.parse(PLANE_JAVA.getJavaContents()));
        assertEquals(expected, parser.findImplementedInterfaces());

    }

    @Test
    public void testIDPFindExtendedClasses() {
        List<String> expected = new ArrayList<>();
        expected.add("Plane");
        parser = new ImportDiagramParser(SUPERPLANE_JAVA);
        parser.setParser(StaticJavaParser.parse(SUPERPLANE_JAVA.getJavaContents()));
        assertEquals(expected, parser.findExtendedClasses());
    }

    @Test
    public void testIDPFindAllExplicitImports() {
        parser = new ImportDiagramParser(FLYER_APP_JAVA);
        parser.setParser(StaticJavaParser.parse(FLYER_APP_JAVA.getJavaContents()));
        assertEquals(4, parser.findAllExplicitImports().size());
        assertEquals("model.Launcher", parser.findAllExplicitImports().get(0).getNameAsString());
        assertEquals("model.Plane", parser.findAllExplicitImports().get(1).getNameAsString());
        assertEquals("model.Seagull", parser.findAllExplicitImports().get(2).getNameAsString());
        assertEquals("model.Flyer", parser.findAllExplicitImports().get(3).getNameAsString());
    }

    @Test
    public void testIDPFindAllImportsSanitizeJavaDefaultImports() {
        parser = new ImportDiagramParser(MATH_BIRD_JAVA);
        parser.setParser(StaticJavaParser.parse(MATH_BIRD_JAVA.getJavaContents()));
        assertEquals(0, parser.findAllExplicitImports().size());
    }

    @Test
    public void testIDPFindAllImplicitImports() {
        parser = new ImportDiagramParser(PLANE_JAVA);
        parser.addJavaInput(FLYER_JAVA);
        parser.setParser(StaticJavaParser.parse(PLANE_JAVA.getJavaContents()));
        assertEquals(1, parser.findAllImplicitImports(PLANE_JAVA).size());
        assertEquals("model.Flyer", parser.findAllImplicitImports(PLANE_JAVA).get(0));
    }

    @Test
    public void testIDPFindAllImplicitImports2() {
        parser = new ImportDiagramParser(javaInputs);
        parser.setParser(StaticJavaParser.parse(LAUNCHER_JAVA.getJavaContents()));
        assertEquals(2, parser.findAllImplicitImports(LAUNCHER_JAVA).size());
        assertEquals("model.Plane", parser.findAllImplicitImports(LAUNCHER_JAVA).get(0));
        assertEquals("model.Flyer", parser.findAllImplicitImports(LAUNCHER_JAVA).get(1));
    }

    @Test
    public void testIDPFindAllImplicitImportsNoPackage() {
        JavaInput testSubClass = new JavaInput("bird-flyer", "class Bird extends Flyer {}");
        JavaInput testSuperClass = new JavaInput("flyer-class", "class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSubClass);
        testClasses.add(testSuperClass);
        parser = new ImportDiagramParser(testClasses);
        parser.setParser(StaticJavaParser.parse(testSubClass.getJavaContents()));
        assertEquals(1, parser.findAllImplicitImports(testSubClass).size());
        assertEquals("Flyer", parser.findAllImplicitImports(testSubClass).get(0));
    }

    @Test
    public void testIDPFindAllImplicitImportsEmptyFiles() {
        JavaInput testEmptyClass1 = new JavaInput("empty-1", "");
        JavaInput testEmptyClass2 = new JavaInput("empty-2", "");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testEmptyClass1);
        testClasses.add(testEmptyClass2);
        parser = new ImportDiagramParser(testClasses);
        parser.setParser(StaticJavaParser.parse(testEmptyClass1.getJavaContents()));
        assertEquals(0, parser.findAllImplicitImports(testEmptyClass1).size());
    }

    @Test
    public void testIDPFindSingleAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "class Test { private Bird bird; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(1, map.get("Bird").size());
        assertEquals(1, map.get("Bird").get(0).getArity());
        assertEquals(AssociationType.FIELD, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindSinglePackageAssociation() {
        JavaInput birdInput = new JavaInput("bird", "package bird; class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "import bird.Bird; class Test { private Bird bird; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("bird.Bird"));
        assertEquals(AssociationType.FIELD, map.get("bird.Bird").get(0).getType());
    }

    @Test
    public void testIDPFindSingleAggregationAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "class Test { private Bird bird; // UMLIE_AGGREGATION "
                + "\n }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(1, map.get("Bird").size());
        assertEquals(1, map.get("Bird").get(0).getArity());
        assertEquals(AssociationType.AGGREGATE_FIELD, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindNoAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        parser = new ImportDiagramParser(birdInput);
        parser.setParser(StaticJavaParser.parse(birdInput.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(0, map.size());
    }

    @Test
    public void testIDPFindTwoSingleAssociations() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("two-singles",
                "class Test { private Bird bird; private Bird " + "friendBird; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(2, map.get("Bird").get(0).getArity());
    }

    @Test
    public void testIDPFindListAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("list", "class Test { private List<Bird> birds; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        parser.setCurrentJavaInput(listAssociation);
        parser.setParser(StaticJavaParser.parse(listAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindListAggregateAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("list", "class Test { private List<Bird> birds; // "
                + "UMLIE_AGGREGATION \n }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        parser.setCurrentJavaInput(listAssociation);
        parser.setParser(StaticJavaParser.parse(listAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(AssociationType.AGGREGATE_LIST_OF, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindArrayListAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("arrayList", "class Test { private ArrayList<Bird> birds; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        parser.setCurrentJavaInput(listAssociation);
        parser.setParser(StaticJavaParser.parse(listAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindListAssociationAndSingle() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single",
                "class Test { private List<Bird> birds; private Bird " + "friend; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(2, map.get("Bird").size());
        assertEquals(1, map.get("Bird").get(0).getArity());
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
        assertEquals(AssociationType.FIELD, map.get("Bird").get(1).getType());
    }

    @Test
    public void testIDPFindSetAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single",
                "class Test { private Set<Bird> birds; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Bird"));
        assertEquals(1, map.get("Bird").size());
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
    }

    @Test
    public void testIDPFindMapAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single",
                "class Test { private Map<Bird, String> birds; }");
        JavaInput otherOrder = new JavaInput("list2",
                "class Test2 { private Map<String, Bird> birds; }");
        JavaInput neither = new JavaInput("list3",
                "class Test3 { private Map<String, String> birds; }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        parser.addJavaInput(otherOrder);
        parser.addJavaInput(neither);
        parser.setCurrentJavaInput(singleAssociation);
        parser.setParser(StaticJavaParser.parse(singleAssociation.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertTrue(map.containsKey("Bird"));
        assertEquals(1, map.get("Bird").size());
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
        parser.setCurrentJavaInput(otherOrder);
        parser.setParser(StaticJavaParser.parse(otherOrder.getJavaContents()));
        map = parser.findAllAssociations();
        assertTrue(map.containsKey("Bird"));
        assertEquals(1, map.get("Bird").size());
        assertEquals(AssociationType.LIST_OF, map.get("Bird").get(0).getType());
        parser.setCurrentJavaInput(neither);
        parser.setParser(StaticJavaParser.parse(neither.getJavaContents()));
        map = parser.findAllAssociations();
        assertFalse(map.containsKey("Bird"));
    }

    @Test
    public void testIDPFindNoAssociations() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(birdInput);
        parser.setParser(StaticJavaParser.parse(birdInput.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(0, map.size());
    }

    @Test
    public void testIDPSkipStatic() {
        JavaInput birdInput = new JavaInput("bird", "class Bird { private static Bird b; }");
        parser = new ImportDiagramParser(birdInput);
        parser.setParser(StaticJavaParser.parse(birdInput.getJavaContents()));
        Map<String, List<AssociationRelation>> map = parser.findAllAssociations();
        assertEquals(0, map.size());
    }

    @Test
    public void testIDPAddFile() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.addJavaInput(PLANE_JAVA);
        List<JavaInput> expected = new ArrayList<>(Arrays.asList(FLYER_JAVA, PLANE_JAVA));
        assertEquals(expected, parser.getJavaInputs());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            Event e = events.next();
            if (eventSize == 1) {
                assertEquals("JavaInput with name flyer-interface added.", e.getDescription());
            } else if (eventSize == 2) {
                assertEquals("JavaInput with name plane-class added.", e.getDescription());
            }
        }
        assertEquals(2, eventSize);
    }

    @Test
    public void testIDPRemoveFile() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.addJavaInput(PLANE_JAVA);
        parser.removeJavaInput(PLANE_JAVA);
        List<JavaInput> expected = new ArrayList<>();
        expected.add(FLYER_JAVA);
        assertEquals(expected, parser.getJavaInputs());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            Event e = events.next();
            if (eventSize == 3) {
                assertEquals("JavaInput with name plane-class removed.", e.getDescription());
            }
        }
        assertEquals(3, eventSize);
    }

    @Test
    public void testIDPRemoveNonExistentFile() {
        parser = new ImportDiagramParser(FLYER_JAVA);
        parser.removeJavaInput(PLANE_JAVA);
        List<JavaInput> expected = new ArrayList<>();
        expected.add(FLYER_JAVA);
        assertEquals(expected, parser.getJavaInputs());
    }

    @Test
    public void testIDPToJson() {
        JSONArray expectedArray = new JSONArray();
        expectedArray.put(FLYER_JAVA.toJson());
        JSONObject expected = new JSONObject();
        expected.put("javaInputs", expectedArray);
        parser = new ImportDiagramParser(FLYER_JAVA);
        assertEquals(expected.toString(), parser.toJson().toString());
    }

    @Test
    public void testIDPClearJavaInputs() {
        parser = new ImportDiagramParser(javaInputs);
        assertEquals(5, parser.getJavaInputs().size());
        parser.clearJavaInputs();
        assertEquals(0, parser.getJavaInputs().size());
        Iterator<Event> events = EventLog.getInstance().iterator();
        int eventSize = -1;
        while (events.hasNext()) {
            eventSize++;
            Event e = events.next();
            if (eventSize == 6) {
                assertEquals("JavaInputs cleared.", e.getDescription());
            }
        }
        assertEquals(6, eventSize);
    }
}
