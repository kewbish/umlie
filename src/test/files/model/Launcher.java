package files.model;

public class Launcher {
    public void launch(Flyer flyer) {
        System.out.println("Go fly, oh flyer!");
        flyer.fly();
    }

    public void refuel(Plane plane) {
        System.out.println("Refueling the plane...");
    }
}
