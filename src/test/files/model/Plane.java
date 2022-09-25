package files.model;

public class Plane implements Flyer {
    @Override
    public void fly() {
        System.out.println("Cruising at 35,000 ft...");
    }
}
