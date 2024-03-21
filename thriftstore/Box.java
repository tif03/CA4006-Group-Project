package thriftstore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import java.util.*;

// class defining features and methods of the box
public class Box {
    public Semaphore mutex;
    public Map<String, Integer> items = new Hashtable<>();

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
        int prev = items.get(section);
        items.put(section, prev + numItems);
    }

    public void removeItems(String section, int numItems) {
        int prev = items.get(section);
        items.put(section, prev - numItems);
    }
}