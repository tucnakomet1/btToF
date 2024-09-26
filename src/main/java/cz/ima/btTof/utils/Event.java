package cz.ima.btTof.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to represent an event
 */
public class Event {
    private final AtomicBoolean flag = new AtomicBoolean(false);

    /** Constructor */
    public Event() {}

    /**
     * Method to check if the event is set
     * @return true if the event is set, false otherwise
     */
    public boolean isSet() {
        return flag.get();
    }

    /** Method to set the event */
    public void set() {
        flag.set(true);
    }

    /** Method to clear the event */
    public void clear() {
        flag.set(false);
    }
}
