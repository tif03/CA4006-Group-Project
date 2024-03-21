package thriftstore;

import java.util.concurrent.Semaphore;

// class defining features and methods of section
public class Section {
    public Semaphore sect_mutex = new Semaphore(1);
    public String section_name;
    public int num_items;

    // Constructor to initalize each section
    Section(String name, int items){
        this.section_name = name;
        this.num_items = items;
    }

    // get section name
    public String getSectionName() {
        return section_name;
    }

    // get number of items 
    public int getNumItems() {
        return num_items;
    }  

    // method to enter the section
    public void enterSect() {
        try {
            sect_mutex.acquire();
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    // method to exit the section
    public void exitSect() {
        sect_mutex.release();
    }

}