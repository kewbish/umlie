package ui.components;

import model.Event;
import model.EventLog;

import java.awt.event.WindowAdapter;

// a window adapter to log events to the console before quitting
public class EventLogWindowAdapter extends WindowAdapter {
    // EFFECTS: prints all events in the event log to the console
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        System.out.println("UMLie Event Log:");
        for (Event ev : EventLog.getInstance()) {
            System.out.println(ev.toString());
        }
        System.exit(0);
    }
}
