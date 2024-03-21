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
    public static Boolean has_items = false;

    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
    public static final int AVERAGE_DELIVERY_INTERVAL = 100;
    public static final int AVERAGE_PURCHASE_INTERVAL = 10;
    public static final int MAX_ITEMS_PER_DELIVERY = 10;
    public static final int MAX_ITEMS_ASSISTANT_CARRY = 10;

    public static final int NUM_CUSTOMERS = 6;

    public static Random random = new Random(123);

    // Define global variables to keep track of sections and items
    public static Box box = new Box(SECTION_NAMES);
    public static List<Section> store = new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //method to make delivery -- it only populates the box
    public static synchronized void delivery() {
        box.enter();

        for (int i = 1; i <= 10; i++) {
            String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
            box.addItem(section, 1);
        }

        System.out.println("<" + Main.ticks.get() + ">" + "<" + Thread.currentThread().getId()+ ">" + "Deposit_of_items : " + box.items);

        box.exit();
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

            System.out.println(ticks.get());
            if (ticks.get() % 100 == 0) {
                delivery();
            }
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
        public synchronized void run() {
            System.out.println("Assistant Thread started");
            while (true) {
        
                // assistant grabs 10 random items from box
                if (delivery_made) {
                    box.enter();
                    has_items = true;

                    System.out.println("Assistant picks up delivery");
        
                    for (int items = 0; items < MAX_ITEMS_ASSISTANT_CARRY; items++) {
                        String randomSection = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
        
                        int temp = assistant_inventory.get(randomSection); // previous inventory value
        
                        if (box.items.get(randomSection) > 0) {
                            box.removeItems(randomSection, 1);
                            assistant_inventory.put(randomSection, temp + 1); // increment
                        } else {
                            items--;
                        }
                    }

                    delivery_made = false; // once an assistant accounts for a delivery back to false until next one
                    box.exit();
                }
        
                // TODO ticks and stuff
        
                if (has_items) {
                    System.out.println("Assistant begins stocking shelves");
                    // stocking shelves
                    for (Entry<String, Integer> entry : ((Hashtable<String, Integer>) assistant_inventory).entrySet()){
                        String inv_section_name = entry.getKey();
                        int inv_num_items = entry.getValue();

                        // TODO enter section
                        Section enter_section = findSection(inv_section_name);
                        enter_section.enterSect();
                        System.out.println("<" + ticks.get() +"> <" + Thread.currentThread().getId() + "> Assistant=1" + " began_stocking_section : " + inv_section_name);

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
                        enter_section.exitSect();
                        // TODO print
                        System.out.println("<" + ticks.get() +"> <" + Thread.currentThread().getId() + "> Assistant=1" + " finished_stocking_section : " + inv_section_name);
                    }
                    has_items = false;
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
    

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void main(String[] args) {

        ticks = new AtomicInteger(0); // set clock to 0

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
