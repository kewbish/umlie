package files.ui;

import files.model.Flyer;
import files.model.Launcher;
import files.model.Plane;
import files.model.Seagull;

// these classes are only for local testing in later phases
public class FlyerApp {
    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        Flyer plane = new Plane();
        Flyer seagull = new Seagull();
    }
}
