package thriftstore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

// class defining features and methods of the box
public class Box {
    public Semaphore mutex;
    public ConcurrentHashMap<String, Integer> items = new ConcurrentHashMap<>();

    // Constructor to initialize the section items
    public Box(String[] sectionNames) {
        this.mutex = new Semaphore(1);
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

    public void addItem(String section, int numItems) {
        synchronized (items) {
            items.put(section, items.getOrDefault(section, 0) + numItems);
        }
    }

    public void removeItems(String section, int numItems) {
        synchronized (items) {
            int currentItems = items.getOrDefault(section, 0);
            items.put(section, Math.max(currentItems - numItems, 0));
        }
    }
}