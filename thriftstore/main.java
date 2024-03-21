package thriftstore;
import java.util.concurrent.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

// TODO -- figure out how variables translate to respective class

public class Main {
    public static AtomicInteger ticks;

    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    public static final int SECTION_NUM = 6;

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
    public static Store store;


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //method to make delivery -- it only populates the box
    public static synchronized void delivery(){
        int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
        int section_index = 0;
        while (remain >= 0 && section_index < SECTION_NUM){
            int items = random.nextInt(remain + 1); // generate random number 0 to remaining space
            String section = SECTION_NAMES[section_index];
            box.addItem(section, items); // putting items into the box
            remain -= items;

            section_index += 1;
        }

        // put remaining items into a random section
        if (remain > 0){
            String section = SECTION_NAMES[random.nextInt(SECTION_NUM)];
            box.addItem(section, remain); // putting items into the box
        }
    }

    // TODO figure out if this actually works
    // in charge of ONLY incrementing ticks, runs in background
    public static void simulate() {
        ticks = new AtomicInteger(0); // set clock to 0
        while (ticks.get() < TOTAL_TICKS_PER_DAY) { // terminate after a day
            try {
                Thread.sleep(TICK_DURATION_MILLISECONDS);
                // increment tick
                ticks.incrementAndGet();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            


        }
    }

    public static synchronized Section findSection(String sect_name){
        for (Section section : store.sections){
            if (section.section_name.equals(sect_name)){
                return section;
            }
        }

        return null; 
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Assistant implements Runnable {
    
        private ConcurrentHashMap<String, Integer> assistant_inventory = new ConcurrentHashMap<>();
    
        private long getId = Thread.currentThread().getId();
    
        // initialize the assistant -- inventory is 0
        public Assistant() {
            for (String section : SECTION_NAMES) {
                assistant_inventory.put(section, 0);
            }
        }
        
    
        @Override
        public void run() {
            while (true) {
    
                // assistant enters box
                box.enter();
    
                // assistant grabs 10 random items from box
                for (int items = 0; items < MAX_ITEMS_ASSISTANT_CARRY; items++) {
                    String randomSection = SECTION_NAMES[random.nextInt(SECTION_NUM)];
                    Section section = findSection(randomSection);
    
                    int temp = assistant_inventory.get(randomSection); // previous inventory value
    
                    if (section.num_items > 0) {
                        box.removeItems(randomSection, 1);
                        assistant_inventory.put(randomSection, temp + 1); // increment
                    } else {
                        items--;
                    }
                }
    
                // assistant exits box
                box.exit();
    
                // assistant walks over to sections from box
                try {
                    Thread.sleep(10 * TICK_DURATION_MILLISECONDS + MAX_ITEMS_ASSISTANT_CARRY * TICK_DURATION_MILLISECONDS);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
    
                int remain = MAX_ITEMS_ASSISTANT_CARRY; 
                for (Entry<String, Integer> entry : assistant_inventory.entrySet()){
                    String inv_section_name = entry.getKey();
                    int inv_num_items = entry.getValue();
    
                    if (inv_num_items > 0) {
    
                        Section stock_sect = store.getSection(inv_section_name);
                    
                        // assistant enters section
                        stock_sect.enterSect();
    
                        stock_sect.num_items += inv_num_items;
    
                        remain -= inv_num_items;
    
                        assistant_inventory.put(inv_section_name, 0); // clear the assistant's inventory for this section
    
                        stock_sect.exitSect();
    
                        // assistant moves from section to section 
                        try {
                            Thread.sleep(10 * TICK_DURATION_MILLISECONDS + remain * TICK_DURATION_MILLISECONDS);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
    
                    }
                }
            }
        }
    }

    static class Customer implements Runnable {
        private final int id;
    
        public long getId = Thread.currentThread().getId();
    
        public Customer(int id) {
            this.id = id;
        }
    
        @Override
        public void run() {
            while (true) {
                // choose random section to visit
                String sectionVisit = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
    
                // find the section object corresponding to the chosen section name
                Section section = findSection(sectionVisit);

                // int start_time = ticks.get();
    
                // customer enters a section
                section.enterSect();
    
                // purchase is triggered -- if empty it does nothing, if has items it makes purchase
                // decrement num items in section
                if (section.num_items > 0) {
                    try {
                        Thread.sleep(TICK_DURATION_MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    section.num_items--;
                    // int finish_time = ticks.get();
                    System.out.println("<" + ticks + ">" + "<" + Thread.currentThread().getId() + "> Customer = " + this.id  + " Collected_from_section : " + sectionVisit);
                    //  + " Waited_ticks : " + (finish_time - start_time)
                }
                
                // customer exits
                section.exitSect();
            }
        }
    } 


    static class Delivery implements Runnable {
    private long getId = Thread.currentThread().getId();

    public void run() {
        while (true) {
            try {
                    Thread.sleep(TICK_DURATION_MILLISECONDS * 100);
                    delivery();

                    System.out.print("<" + ticks + ">" + "<" + Thread.currentThread().getId() + ">" + "Deposit_of_items : ");
                    for (Entry<String, Integer> item : box.items.entrySet()){
                        String section = item.getKey();
                        int num_items = item.getValue();
                        if (num_items > 0){
                            System.out.print(section + " = " + num_items + " "); 
                        }
                    }
                    System.out.println();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void main(String[] args) {
        box = new Box(SECTION_NAMES);
        store = new Store();

        List<Thread> customer_threads = new ArrayList<>();

        // Main Delivery thread
        Thread deliveryThread = new Thread(new Delivery());
        deliveryThread.start();

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

        deliveryThread.interrupt();
        assistant.interrupt();
        for (int i = 0; i < NUM_CUSTOMERS; i++) {
            customer_threads.get(i).interrupt();
        } 
    }
 
}
