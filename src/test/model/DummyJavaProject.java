package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// exports common constants for TestUMLangGenerator and TestImportDiagramParser
public class DummyJavaProject {
    // Code adapted from EDX A4 Lecture Ticket Archive
    // https://learning.edge.edx.org/course/course-v1:UBC+CPSC210+all/block-v1:UBC+CPSC210+all+type@sequential+block@00477d6854fd4fadbf9d1b3955f2f6ff/block-v1:UBC+CPSC210+all+type@vertical+block@c66e75437ff0423eb0fbcd287bab8563:w
    public static final JavaInput FLYER_JAVA = new JavaInput("flyer-interface",
            "package model;\n\npublic interface Flyer {\n        public void fly();" + "\n" + "    }");
    public static final JavaInput PLANE_JAVA = new JavaInput("plane-class",
            "package model;\n" + "public class Plane implements Flyer {\n" + "   " + "@Override\n"
                    + "   public void fly() {\n" + "       System.out.println(\"Cruising at 35,000 ft...\");\n"
                    + "   }\n" + "}\n");
    public static final JavaInput SUPERPLANE_JAVA = new JavaInput("superplane",
            "package model;\n" + "public class Superplane extends Plane {\n" + "   " + "@Override\n"
                    + "   public void fly() {\n" + "       System.out.println(\"Cruising at 105,000 ft...\");\n"
                    + "   }\n" + "}\n");
    public static final JavaInput MATH_BIRD_JAVA = new JavaInput("math-bird",
            "package model;\n" + "import java.lang.Math;\n" + "import org.javaparser.Sanitizer;\n" + "import ca.ubc"
                    + ".students.github.SSC;\n" + "import com.github.mathpackage.Multiplication;" + "\n" + "public "
                    + "class" + " " + "Mathbird " + "extends " + "Seagull {\n" + "   @Override\n"
                    + "   public void fly() {\n"
                    + "       System.out.println(\"Cruising at \" + Math.max(30000,50000) + \"ft...\");\n" + "   }\n"
                    + "}\n");
    public static final JavaInput SEAGULL_JAVA = new JavaInput("seagull",
            "package model;\n" + "\n" + "public class Seagull implements Flyer {\n" + "   " + "@Override\n"
                    + "   public void fly() {\n" + "       System.out.println(\"Soaring with the wind...\");\n"
                    + "   }\n" + "}");
    public static final JavaInput LAUNCHER_JAVA = new JavaInput("launcher",
            "package model;\n" + "\n" + "public class Launcher {\n" + "   public void launch" + "(Flyer " + "flyer){\n"
                    + "       System.out.println(\"Go fly, oh flyer!\");\n" + "       flyer.fly();\n" + "   }\n" + "\n"
                    + "   public void refuel(Plane plane){\n"
                    + "       System.out.println(\"Refueling the plane...\");\n" + "   }\n" + "}");
    public static final JavaInput FLYER_APP_JAVA = new JavaInput("flyer-app",
            "package ui;\n" + "import model.Launcher;\n" + "import model.Plane;\n" + "import model.Seagull;\n"
                    + "import model.Flyer;\n" + "\n" + "\n" + "public class FlyerApp {\n"
                    + "   public static void main(String[] args) {\n" + "       Launcher launcher = new Launcher();\n"
                    + "       Flyer plane = new Plane();\n" + "       Flyer seagull = new Seagull();\n" + "   }\n"
                    + "}");
    public static final JavaInput OTHER_PACKAGE_BIRD = new JavaInput("bird-package-bird",
            "package birds;\n" + "import model.Flyer;" + "public class EtherealBird " + "implements Flyer {\n" + "   "
                    + "@Override\n" + "   public void fly() {\n"
                    + "       System.out.println(\"Soaring in the galaxy...\");\n" + "   }\n" + "}");
    public static final List<JavaInput> javaInputs = new ArrayList<>(
            Arrays.asList(FLYER_JAVA, PLANE_JAVA, SEAGULL_JAVA, LAUNCHER_JAVA, FLYER_APP_JAVA));
}
