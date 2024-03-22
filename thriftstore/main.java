package thriftstore;
import java.util.concurrent.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

import thriftstore.Main.Assistant;

public class Main {
    public static AtomicInteger ticks;

    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    public static final int SECTION_NUM = 6;

    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
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

    // in charge of incrementing ticks, runs in background
    public static void simulate() {
        while (ticks.get() <= TOTAL_TICKS_PER_DAY) { // terminate after a day

            try {
                Thread.sleep(TICK_DURATION_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ticks.incrementAndGet();

            Random random = new Random();
            if (random.nextDouble() <= 0.01) {
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
        private final int id;
    
        public static Map<String, Integer> assistant_inventory = new HashMap<>();
    
        private int MAX_ITEMS_ASSISTANT_CARRY = 10;
    
        // initialize the assistant -- inventory is 0
        public Assistant(int id) {
            this.id = id;
            for (String section : SECTION_NAMES) {
                assistant_inventory.put(section, 0);
            }
        }

        public static Boolean inventoryEmpty(){
            for (Map.Entry<String, Integer> entry : assistant_inventory.entrySet()){
                Integer num = entry.getValue();

                if (num > 0){
                    return false;
                }
            }

            return true;
        }

        public static int getInventoryValue() {
            int sum = 0;
            for (Map.Entry<String, Integer> entry : assistant_inventory.entrySet()){
                Integer num = entry.getValue();

                sum += num;
            }
            return sum;
        }
    
    
        @Override
        public synchronized void run() {
            while (true) {
                // assistant grabs 10 random items from box

                if (!box.boxEmpty()) {
                    box.enter();

        
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
                    box.exit();
                }
        
                // walk over to sections
                try {
                    Thread.sleep(10 * TICK_DURATION_MILLISECONDS + MAX_ITEMS_ASSISTANT_CARRY * TICK_DURATION_MILLISECONDS); // 10 seconds + amount of items carrying
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        
                if (!inventoryEmpty()) {

                    int inventory = 0;
    
                    // stocking shelves
                    for (Entry<String, Integer> entry : assistant_inventory.entrySet()){
                        String inv_section_name = entry.getKey();
                        int inv_num_items = entry.getValue();

                        // enter section
                        Section enter_section = findSection(inv_section_name);
                        enter_section.enterSect();
                        System.out.println("<" + ticks.get() +"> <" + Thread.currentThread().getId() + "> Assistant=" + id + " began_stocking_section : " + inv_section_name);

                        if (inv_num_items > 0) {

                            Section temp_section = findSection(inv_section_name);

                            // assistant stocks the thrift store shelves with items in inventory
                            for (Section section : store){
                                if (section.section_name.equals(temp_section.section_name)){
                                    temp_section.addToSection(inv_num_items);
                                }
                            }

                            try {
                                Thread.sleep(inv_num_items * TICK_DURATION_MILLISECONDS); // 10 seconds + amount of items carrying
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            

                            assistant_inventory.put(inv_section_name, 0); // clear the assistant's inventory for this section
                            inventory = getInventoryValue();
                        }

                        System.out.println("<" + ticks.get() +"> <" + Thread.currentThread().getId() + "> Assistant=1" + " finished_stocking_section : " + inv_section_name);
                        // exit section
                        enter_section.exitSect();
            
                        
                        try {
                            Thread.sleep(10 * TICK_DURATION_MILLISECONDS + inventory * TICK_DURATION_MILLISECONDS); // 10 seconds + amount of items carrying
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            }
        }
    }
    

    static class Customer implements Runnable {

        public static Random random = new Random();
        private final int id;
    
        public Customer(int id) {
            this.id = id;
        }
    
        @Override
        public synchronized void run() {
            while (true) {
                // choose random section to visit
                String sectionVisit;
                Section enter_section;

                do {
                    sectionVisit = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
                    enter_section = findSection(sectionVisit);
                } while (enter_section.num_items == 0); // keep selecting until non-empty section found

                int start_time = ticks.get();

                if (enter_section.num_items > 0){    
                    enter_section.enterSect();
                    
                    for (Section section : store){
                        if (enter_section.num_items > 0 && section.section_name.equals(sectionVisit)){
                            section.subFromSection(1);

                            int finish_time = ticks.get();

                            // 1 tick to grab
                            try {
                                Thread.sleep(TICK_DURATION_MILLISECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            System.out.println("<" + ticks.get() +"> <" + Thread.currentThread().getId() + "> Customer=" + id + " collected_from_section : " + sectionVisit + " waited_ticks : " + (finish_time - start_time));
                        }
                    }
                    
                    // exit section
                    enter_section.exitSect();

                    try {
                        long sleepDuration = (long) (10 + (random.nextGaussian() * 3));
                        Thread.sleep(sleepDuration * TICK_DURATION_MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    } 
    

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        // set clock to 0
        ticks = new AtomicInteger(0); 

        // initializes every section with 5 items at the start
        for (String section_name : SECTION_NAMES) {
            store.add(new Section (section_name, 5));
        } 

        List<Thread> assistant_threads = new ArrayList<>();
        List<Thread> customer_threads = new ArrayList<>();

        // starting Assistant threads
        for (int i = 1; i < 2; i++) {
            Thread assistant = new Thread(new Assistant(i));
            assistant_threads.add(assistant);
            assistant.start();
        }

        // starting Customer threads
        for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
            Thread customer = new Thread(new Customer(i));
            customer_threads.add(customer);
            customer.start();
        }

        // start the simulation + clock
        simulate(); 

        // starting Assistant threads
        for (Thread assistant : assistant_threads) {
            assistant.stop();
        }

        // starting Customer threads
        for (Thread customer : customer_threads) {
            customer.stop();
        }


    }
 
}

