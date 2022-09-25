package model;

import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import persistence.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static model.DummyJavaProject.*;
import static org.junit.jupiter.api.Assertions.*;

public class UMLangGeneratorTest {
    // constructor tests
    @Test
    public void testUMLangGeneratorConstructor() {
        ImportDiagramParser parser = new ImportDiagramParser();
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals(parser, generator.getParser());
    }

    // class import diagram tests
    @Test
    public void testUMLangClassImportDiagramNoFiles() {
        ImportDiagramParser parser = new ImportDiagramParser();
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml", generator.generateClassImportDiagram());
    }

    @Test
    public void testUMLangClassImportDiagramOneFile() {
        ImportDiagramParser parser = new ImportDiagramParser(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\ninterface model.Flyer\n@enduml", generator.generateClassImportDiagram());
    }

    @Test
    public void testUMLangClassImportDiagramMultipleFiles() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "interface model.Flyer\n" + "class model.Plane\n" + "model.Plane -> model.Flyer\n"
                + "class model.Seagull\n" + "model.Seagull -> model.Flyer\n" + "class model.Launcher\n"
                + "model.Launcher -> model.Plane\n" + "model.Launcher -> model" + ".Flyer\n" + "class ui.FlyerApp\n"
                + "ui.FlyerApp -> model.Launcher\n" + "ui.FlyerApp -> model.Plane\n" + "ui.FlyerApp -> model"
                + ".Seagull\n" + "ui.FlyerApp -> model.Flyer\n" + "@enduml", generator.generateClassImportDiagram());
    }

    @Test
    public void testUMLangClassImportNoFiles() {
        ImportDiagramParser parser = new ImportDiagramParser();
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml", generator.generateClassImportDiagram());
    }

    @Test
    public void testUMLangClassImportNoPackage() {
        ImportDiagramParser parser = new ImportDiagramParser(new JavaInput("no-package-bird", "class Bird {}"));
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\nclass Bird\n@enduml", generator.generateClassImportDiagram());
    }

    // class relationship diagram tests
    @Test
    public void testUMLangClassRelationshipOneFile() {
        ImportDiagramParser parser = new ImportDiagramParser(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\ninterface model.Flyer\n@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipOneFileMultiplePackages() {
        ImportDiagramParser parser = new ImportDiagramParser(OTHER_PACKAGE_BIRD);
        parser.addJavaInput(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "class birds.EtherealBird\n" + "birds.EtherealBird ..|> model.Flyer\n"
                + "interface model.Flyer\n" + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipMultipleFiles() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals(
                "@startuml\n"
                        + "interface model.Flyer\n"
                        + "class model.Plane\n"
                        + "model.Plane ..|> model.Flyer\n"
                        + "class model.Seagull\n"
                        + "model.Seagull ..|> model.Flyer\n"
                        + "class model.Launcher\n"
                        + "class ui.FlyerApp\n"
                        + "ui.FlyerApp -.-> model.Seagull\n"
                        + "ui.FlyerApp -.-> model.Plane\n"
                        + "ui.FlyerApp -.-> model.Launcher\n"
                        + "ui.FlyerApp -.-> model.Flyer\n"
                        + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipMultipleFilesNoDeps() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        UMLangGenerator generator = new UMLangGenerator(parser);
        generator.setShowDependencies(false);
        assertEquals(
                "@startuml\n"
                        + "interface model.Flyer\n"
                        + "class model.Plane\n"
                        + "model.Plane ..|> model.Flyer\n"
                        + "class model.Seagull\n"
                        + "model.Seagull ..|> model.Flyer\n"
                        + "class model.Launcher\n"
                        + "class ui.FlyerApp\n"
                        + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipExplicitUnused() {
        JavaInput testSubClass = new JavaInput("unused-import",
                "import Flyer; import UnusedClass; class Bird extends " + "Flyer {}");
        JavaInput testSuperClass = new JavaInput("flyer-class", "class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "class Flyer\n" + "class Bird\n" + "Bird --|> Flyer\n" + "@enduml",
                generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipExplicitImportExtends() {
        JavaInput testSubClass = new JavaInput("bird-class", "import Flyer; class Bird extends " + "Flyer {}");
        JavaInput testSuperClass = new JavaInput("flyer-class", "class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "class Flyer\n" + "class Bird\n" + "Bird --|> Flyer\n" + "@enduml",
                generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipExplicitNestedImportExtends() {
        JavaInput testSubClass = new JavaInput("extends-far-away-flyer",
                "import far.away.Flyer; class Bird extends Flyer " + "{}");
        JavaInput testSuperClass = new JavaInput("far-away-flyer", "package far.away; class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "class far.away.Flyer\n" + "class Bird\n" + "Bird --|> far.away" + ".Flyer\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipExplicitImportImplements() {
        JavaInput testSubClass = new JavaInput("bird-class", "import Flyer; class Bird implements " + "Flyer {}");
        JavaInput testSuperClass = new JavaInput("flyer-class", "interface Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "interface Flyer\n" + "class Bird\n" + "Bird ..|> Flyer\n" + "@enduml",
                generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipExplicitNestedImportImplements() {
        JavaInput testSubClass = new JavaInput("bird-class",
                "import far.away.Flyer; class Bird implements " + "Flyer " + "{}");
        JavaInput testSuperClass = new JavaInput("flyer-class", "package far.away; interface Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "interface far.away.Flyer\n" + "class Bird\n" + "Bird ..|> far" + ".away.Flyer\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipNonExtendedImplicitImport() {
        JavaInput testSubClass = new JavaInput("bird-class",
                "import Flyer; class Bird implements Flyer { public void" + " fly(Plane plane) {} }");
        JavaInput testSuperClass = new JavaInput("flyer-interface", "interface Flyer {}");
        JavaInput testUnusedClass = new JavaInput("plane-class", "class Plane {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        testClasses.add(testUnusedClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals(
                "@startuml\n" + "interface Flyer\n" + "class Bird\n" + "Bird ..|> Flyer\n" + "Bird -.-> "
                        + "Plane\n" + "class Plane\n" +
                        "@enduml",
                generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipImplicitImportExtends() {
        JavaInput testSubClass = new JavaInput("bird-class", "class Bird extends Flyer { }");
        JavaInput testSuperClass = new JavaInput("flyer", "class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testSuperClass);
        testClasses.add(testSubClass);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n" + "class Flyer\n" + "class Bird\n" + "Bird --|> Flyer\n" + "@enduml",
                generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangClassRelationshipNoPackage() {
        ImportDiagramParser parser = new ImportDiagramParser(new JavaInput("bird", "class Bird {}"));
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }


    @Test
    public void testUMLangSingleAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "class Test { private Bird bird; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"1\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangSingleAggregationAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "class Test { private Bird bird; // UMLIE_AGGREGATION "
                + "\n }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test o--> \"1\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangTwoSingleAssociations() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("two-singles",
                "class Test { private Bird bird; private Bird " + "friendBird; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"2\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangListAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("list", "class Test { private List<Bird> birds; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"0..*\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangListAggregationAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("list", "class Test { private List<Bird> birds; // "
                + "UMLIE_AGGREGATION \n }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test o--> \"0..*\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangTwoListAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput listAssociation = new JavaInput("list", "class Test { private List<Bird> birds; private List<Bird> "
                + "nonFriendlyBirds; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(listAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"0..*\" Bird\n"
                + "Test --> \"0..*\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangFindListAssociationAndSingle() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single",
                "class Test { private List<Bird> birds; private Bird " + "friend; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"0..*\" Bird\n"
                + "Test --> \"1\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangSetAssociation() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single",
                "class Test { private Set<Bird> birds; private Bird " + "friend; }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test --> \"0..*\" Bird\n"
                + "Test --> \"1\" Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangAssociationEnums() {
        JavaInput birdInput = new JavaInput("bird", "public enum AssociationType {\n"
                + "    FIELD, AGGREGATE_FIELD, LIST_OF, AGGREGATE_LIST_OF, DEPENDENCY\n"
                + "}\n");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        UMLangGenerator generator = new UMLangGenerator(parser);
        parser.setParser(StaticJavaParser.parse(birdInput.getJavaContents()));
        assertEquals("@startuml\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Test
    public void testUMLangAssociationEnumImports() {
        JavaInput birdInput = new JavaInput("bird", "public enum AssociationType {\n"
                + "    FIELD, AGGREGATE_FIELD, LIST_OF, AGGREGATE_LIST_OF, DEPENDENCY\n"
                + "}\n");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        UMLangGenerator generator = new UMLangGenerator(parser);
        parser.setParser(StaticJavaParser.parse(birdInput.getJavaContents()));
        assertEquals("@startuml\n"
                + "@enduml", generator.generateClassImportDiagram());
    }

    @Test
    public void testUMLangDependency() {
        JavaInput birdInput = new JavaInput("bird", "class Bird {  }");
        JavaInput singleAssociation = new JavaInput("single", "class Test { public Test() { Bird.bird(); } }");
        ImportDiagramParser parser = new ImportDiagramParser(birdInput);
        parser.addJavaInput(singleAssociation);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n"
                + "class Bird\n"
                + "class Test\n"
                + "Test -.-> Bird\n"
                + "@enduml", generator.generateClassRelationshipDiagram());
    }

    @Disabled
    @Test
    public void testUMLangFullProjectLoad() {
        JsonReader reader = new JsonReader("./data/testFullProjectLoad.json");
        try {
            ImportDiagramParser parser = reader.read();
            UMLangGenerator generator = new UMLangGenerator(parser);
            assertEquals("@startuml\n"
                            + "class model.AssociationRelation\n"
                            + "class model.ClassNameCollector\n"
                            + "class model.Event\n"
                            + "class model.EventLog\n"
                            + "model.EventLog --> \"1\" model.EventLog\n"
                            + "class model.ExtendedTypesCollector\n"
                            + "class model.ImplementedTypesCollector\n"
                            + "class model.ImportDiagramParser\n"
                            + "model.ImportDiagramParser ..|> persistence.Writeable\n"
                            + "model.ImportDiagramParser --> \"0..*\" model.JavaInput\n"
                            + "model.ImportDiagramParser --> \"1\" model.JavaInput\n"
                            + "class model.InterfaceNameCollector\n"
                            + "class model.JavaInput\n"
                            + "model.JavaInput ..|> persistence.Writeable\n"
                            + "class model.TypeNameDeclaration\n"
                            + "class model.UMLangGenerator\n"
                            + "model.UMLangGenerator --> \"1\" model.ImportDiagramParser\n"
                            + "class persistence.JsonReader\n"
                            + "class persistence.JsonWriter\n"
                            + "class persistence.UMLangWriter\n"
                            + "persistence.UMLangWriter --> \"1\" model.UMLangGenerator\n"
                            + "interface persistence.Writeable\n"
                            + "class ui.Main\n"
                            + "class ui.UMLieConsoleApp\n"
                            + "ui.UMLieConsoleApp --> \"1\" model.UMLangGenerator\n"
                            + "ui.UMLieConsoleApp --> \"1\" model.ImportDiagramParser\n"
                            + "class ui.UMLieGUI\n"
                            + "ui.UMLieGUI --> \"1\" model.UMLangGenerator\n"
                            + "ui.UMLieGUI --> \"1\" ui.components.SaveUMLangButton\n"
                            + "ui.UMLieGUI --> \"1\" ui.components.SaveImageButton\n"
                            + "ui.UMLieGUI --> \"1\" model.ImportDiagramParser\n"
                            + "class ui.components.EventLogWindowAdapter\n"
                            + "class ui.components.ImageGenerator\n"
                            + "ui.components.ImageGenerator --> \"1\" model.UMLangGenerator\n"
                            + "class ui.components.ImageGeneratorActionListener\n"
                            + "ui.components.ImageGeneratorActionListener --|> ui.components.ImageGenerator\n"
                            + "class ui.components.ImageGeneratorButton\n"
                            + "class ui.components.SaveImageButton\n"
                            + "ui.components.SaveImageButton --|> ui.components.SaveTypeButton\n"
                            + "class ui.components.SaveTypeButton\n"
                            + "ui.components.SaveTypeButton --> \"1\" model.UMLangGenerator\n"
                            + "class ui.components.SaveTypeButtonActionListener\n"
                            + "ui.components.SaveTypeButtonActionListener -.-> \n"
                            + "class ui.components.SaveUMLangButton\n"
                            + "ui.components.SaveUMLangButton --|> ui.components.SaveTypeButton\n"
                            + "@enduml",
                    generator.generateClassRelationshipDiagram());
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

    // package diagram tests
    @Test
    public void testUMLangPackageDiagramNoFiles() {
        ImportDiagramParser parser = new ImportDiagramParser();
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramOneFile() {
        ImportDiagramParser parser = new ImportDiagramParser(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\nfolder model\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramEmptyFiles() {
        ImportDiagramParser parser = new ImportDiagramParser(new JavaInput("empty", ""));
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramMultipleFiles() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\nfolder model\nfolder ui\nui -> model\n@enduml",
                generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramNoPackage() {
        ImportDiagramParser parser = new ImportDiagramParser();
        JavaInput test1 = new JavaInput("test1", "class Bird { }");
        JavaInput test2 = new JavaInput("test2", "import Bird; class Plane { }");
        parser.addJavaInput(test1);
        parser.addJavaInput(test2);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\n@enduml",
                generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramJavaInputs() {
        JsonReader reader = new JsonReader("./data/testFullProjectLoad.json");
        try {
            ImportDiagramParser parser = reader.read();
            UMLangGenerator generator = new UMLangGenerator(parser);
            assertEquals("@startuml\nfolder model\n"
                            + "folder ui.components\n"
                            + "folder persistence\n"
                            + "folder ui\n"
                            + "ui.components -> persistence\n"
                            + "model -> persistence\n"
                            + "ui.components -> model\n"
                            + "ui -> persistence\n"
                            + "persistence -> model\n"
                            + "ui -> model\n@enduml",
                    generator.generateUMLangPackageRelationshipDiagram());
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

    @Test
    public void testUMLangPackageDiagramSanitizeImports() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        JavaInput sanitizeMe = new JavaInput("sanitized", "import java.lang.Math; import com.github"
                + ".Package; import "
                + "net.github.ClassMan; import ca.github.Box; class Bird { private Math math; private Package "
                + "packager; }");
        parser.addJavaInput(sanitizeMe);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\nfolder model\nfolder ui\nui -> model\n@enduml",
                generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangPackageDiagramSelfReferentialPackage() {
        JavaInput testClass1 = new JavaInput("bird-class", "package model; import model.Flyer; class Bird {}");
        JavaInput testClass2 = new JavaInput("flyer", "package model; import model.Bird; class Flyer {}");
        List<JavaInput> testClasses = new ArrayList<>();
        testClasses.add(testClass2);
        testClasses.add(testClass1);
        ImportDiagramParser parser = new ImportDiagramParser(testClasses);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals("@startuml\nfolder model\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }

    // diagram type tests
    @Test
    public void testUMLangPackageDiagramType() {
        ImportDiagramParser parser = new ImportDiagramParser(javaInputs);
        UMLangGenerator generator = new UMLangGenerator(parser);
        assertEquals(generator.generateClassImportDiagram(),
                generator.generateDiagramForType(DiagramType.CLASS_IMPORT));
        assertEquals(generator.generateClassRelationshipDiagram(),
                generator.generateDiagramForType(DiagramType.CLASS_RELATIONSHIP));
        assertEquals(generator.generateUMLangPackageRelationshipDiagram(),
                generator.generateDiagramForType(DiagramType.PACKAGE));
    }

    // setter tests
    @Test
    public void testUMLangSetParser() {
        ImportDiagramParser parser = new ImportDiagramParser(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        ImportDiagramParser emptyParser = new ImportDiagramParser();
        generator.setParser(emptyParser);
        assertEquals("@startuml\n@enduml", generator.generateUMLangPackageRelationshipDiagram());
    }

    @Test
    public void testUMLangSetDependencies() {
        ImportDiagramParser parser = new ImportDiagramParser(FLYER_JAVA);
        UMLangGenerator generator = new UMLangGenerator(parser);
        generator.setParser(parser);
        assertTrue(generator.isShowingDependencies());
        generator.setShowDependencies(false);
        assertFalse(generator.isShowingDependencies());
        generator.setShowDependencies(true);
        assertTrue(generator.isShowingDependencies());
    }
}
