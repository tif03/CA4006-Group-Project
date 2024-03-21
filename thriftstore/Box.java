package thriftstore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

// class defining features and methods of the box
public class Box {
    public Semaphore mutex = new Semaphore(1);
    public ConcurrentHashMap<String, Integer> items = new ConcurrentHashMap<>();

    // Constructor to initialize the section items
    public Box(String[] sectionNames) {
        for (String section : sectionNames) {
            items.put(section, 0); // Initialize each section with 0 items
        }
    }

    // method to enter the box
    public void enter() {
        try {
            mutex.acquire(); // aquire semaphore to go in
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    // method to exit box
    public void exit() {
        mutex.release();
    }

    // method to add a certain number of items to a section of the box
    public void addItem(String section, int numItems) {
        items.put(section, items.get(section) + numItems);
    }

    // method to remove a certain number of items
    public void removeItems(String section, int numItems) {
        items.put(section, Math.max((items.get(section) - numItems), 0));
    }
}