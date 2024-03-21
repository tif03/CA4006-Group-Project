package thriftstore;
import java.util.concurrent.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.*;

import thriftstore.Main.Assistant;

// TODO -- figure out how variables translate to respective class

@SuppressWarnings("deprecation")
public class Main {
    public static AtomicInteger ticks;

    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    public static final int SECTION_NUM = 6;

    public static Boolean delivery_made = false;

    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
    public static final int AVERAGE_DELIVERY_INTERVAL = 100;
    public static final int AVERAGE_PURCHASE_INTERVAL = 10;
    public static final int MAX_ITEMS_PER_DELIVERY = 10;
    public static final int MAX_ITEMS_ASSISTANT_CARRY = 10;

    public static final int NUM_CUSTOMERS = 6;

    public static Random random = new Random();

    // Define global variables to keep track of sections and items
    public static Box box;
    public static List<Section> store;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //method to make delivery -- it only populates the box
    public static synchronized void delivery() {
        int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
        while (remain > 0){

            String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
            int items = random.nextInt(remain + 1);
            box.addItem(section, items); // putting items into the box
            
            remain -= items;
        }

        delivery_made = true;
        System.out.println("<" + Main.ticks.get() + ">" + "<Thread_ID>" + "Deposit_of_items : ");
        for (Entry<String, Integer> entry : box.items.entrySet()) {
            String item = entry.getKey();
            int quantity = entry.getValue();
            System.out.print(item + " = " + quantity + " ");
        } 
        System.out.println();
    }

    // TODO figure out if this actually works
    // in charge of ONLY incrementing ticks, runs in background
    public static void simulate() {
        while (ticks.get() < TOTAL_TICKS_PER_DAY) { // terminate after a day
            try {
                Thread.sleep(TICK_DURATION_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ticks.incrementAndGet();
            
        }
    }

    public static synchronized Section findSection(String sect_name){
        for (Section section : store){
            if (section.section_name.equals(sect_name)){
                return section;
            }
        }

        return null; 
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Assistant implements Runnable {

        public static Random random = new Random();
    
        private Dictionary<String, Integer> assistant_inventory = new Hashtable<>();
    
        private long getId = Thread.currentThread().getId();
        private int MAX_ITEMS_ASSISTANT_CARRY = 10;
    
        // initialize the assistant -- inventory is 0
        public Assistant() {
            for (String section : SECTION_NAMES) {
                assistant_inventory.put(section, 0);
            }
        }
    
    
    @Override
    public void run() {
        while (true) {
    
            // assistant grabs 10 random items from box
            // TODO figure out a semaphore for the box
            if (delivery_made) {
    
                delivery_made = false; // once an assistant accounts for a delivery back to false until next one
    
                for (int items = 0; items < MAX_ITEMS_ASSISTANT_CARRY; items++) {
                    String randomSection = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
    
                    int temp = assistant_inventory.get(randomSection); // previous inventory value
    
                    if (box.items.get(randomSection) > 0) {
                        box.addItem(randomSection, temp - 1);
                        assistant_inventory.put(randomSection, temp + 1); // increment
                    } else {
                        items--;
                    }
                }
            }
    
            // TODO ticks and stuff
    
            int remain = MAX_ITEMS_ASSISTANT_CARRY; 
            for (Entry<String, Integer> entry : ((Hashtable<String, Integer>) assistant_inventory).entrySet()){
                String inv_section_name = entry.getKey();
                int inv_num_items = entry.getValue();
    
                // TODO enter section
    
                if (inv_num_items > 0) {
    
                    Section temp_section = findSection(inv_section_name);
                    int temp = temp_section.num_items;
    
                    // assistant stocks the thrift store shelves with items in inventory
                    for (Section section : store){
                        if (section.section_name.equals(temp_section.section_name)){
                            temp_section.addToSection(inv_num_items);
                        }
                    }
    
                    assistant_inventory.put(inv_section_name, 0); // clear the assistant's inventory for this section
    
                }
    
                // TODO exit section
                // TODO print
            }
        }
    }
    }
    

    static class Customer implements Runnable {

        public static Random random = new Random();
        private final int id;
        // public long getId = Thread.currentThread().getId();
    
        public Customer(int id) {
            this.id = id;
        }
    
        @Override
        public void run() {
            while (true) {
                // choose random section to visit
                String sectionVisit = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
    
                // TODO enter section

                for (Section section : store){
                    if (section.num_items > 0 && section.section_name.equals(sectionVisit)){
                        section.subFromSection(1);
                    }
                }
    
    
                // TODO print
                // TODO exit section
            }
        }
    } 

    static class Delivery implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                // try {
                    // Thread.sleep(TICK_DURATION_MILLISECONDS * 100);
                    
                    box.enter();

                    delivery();
    
                    System.out.print("<" + ticks + ">" + "<" + Thread.currentThread().getName() + ">" + " Deposit_of_items : ");
                    for (Entry<String, Integer> item : box.items.entrySet()){
                        String section = item.getKey();
                        int num_items = item.getValue();
                        if (num_items > 0){
                            System.out.print(section + " = " + num_items + " "); 
                        }
                    }

                    System.out.println();
                    box.exit();
                // } catch (InterruptedException e) {
                //     e.printStackTrace();
                // }
            }
        }
    }
    

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void main(String[] args) {

        ticks = new AtomicInteger(0); // set clock to 0

        box = new Box(SECTION_NAMES);

        // init store

        for (String section_name : SECTION_NAMES) {
            store.add(new Section (section_name, 5));
        } 

        List<Thread> customer_threads = new ArrayList<>();

        // Main Delivery thread
        // Thread deliveryThread = new Thread(new Delivery());
        // deliveryThread.start();

        // initalize assistant thread
        Thread assistant = new Thread(new Assistant());
        assistant.start();

        // initialize customer threads
        for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
            Thread customer = new Thread(new Customer(i));
            customer_threads.add(customer);
            customer.start();
        }

        // Start the simulation + clock
        simulate(); 
    }
 
}
