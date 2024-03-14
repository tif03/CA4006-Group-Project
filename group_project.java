import java.util.concurrent.*;
import java.util.Random;

public class ThriftStore {
    private static int ticks = 1; // clock

    private static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    private static final int SECTION_NUM = 6;

    private static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    private static final int TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
    private static final int AVERAGE_DELIVERY_INTERVAL = 100;
    private static final int AVERAGE_PURCHASE_INTERVAL = 10;
    private static final int MAX_ITEMS_PER_DELIVERY = 10;

    private static final int NUM_CUSTOMERS = 6;

    private Random random = new Random();

    // Define variables to keep track of sections and items
    private static Box box = new Box(SECTION_NAMES);

    // TODO could have inventory be a list of Section
    
    // class defining features and methods of the box
    private static class Box {
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
                mutex.aquire(); // aquire semaphore to go in
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

    // class defining features and methods of section
    // TODO Section -- name, num items; getName, getNum, enter (aquire), exit (release), stock, purchase
    public static class Section(){
        public Semaphore sect_mutex = new Semaphore(1);
        public String section_name;
        public int num_items;

        // Constructor to initalize each section
        Section(String name, int items){
            this.section_name = name;
            this.num_items = items;
        }

    }




    //method to make delivery -- it only populates the box
    private static void delivery(){
        int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
        int section_index = 0;
        while (remain >= 0 && section_index <= SECTION_NUM){
            int items = random.nextInt(remain + 1); // generate random number 0 to remaining space
            String section = SECTION_NAMES[section_index];
            box.addItem(section, items); // putting items into the box
            remain -= items;

            section_index += 1;
        }

        // put remaining items into a random section
        if (remain > 0){
            String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.size())];
            box.addItem(section, items); // putting items into the box
        }

        // TODO Thread ID
        System.out.print("<" + ticks + "> <Thread ID> Deposit_of_items : ");
        for (Entry<String, Integer> item : box.items){
            String section = entry.getKey();
            int num_items = entry.getValue();
            if (num_items > 0){
                System.out.print(section + " = " + num_items + ", "); 
            }
        }
        System.out.println();
    }

    // in charge of incrementing ticks
    public void simulate() {
        while (ticks <= TOTAL_TICKS_PER_DAY) { // terminate after a day
    
            // Check if it's time for a delivery event
            if (random.nextDouble() < 1.0 / AVERAGE_DELIVERY_INTERVAL) {
                delivery(); // delivery event is just to populate the box
            }

            // always signal assistant to stock items
            // stocking event if box is full

            // code to trigger a customer thread

            // Simulate the passing of time
            try {
                Thread.sleep(TICK_DURATION_MILLISECONDS); // Sleep for the tick duration
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ticks++; // Increment the tick count
        }

    }




    // TODO
    class Customer implements Runnable {
        private final int id;

        public Customer(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                // customer enters a section
                // purchase is triggered -- if empty it does nothing, if has items it makes purchase
                // customer exits
            }
        }
    } 

    // TODO
    class Assistant implements Runnable {
        private static ConcurrentHashMap<String, Integer> assistant_inventory = new ConcurrentHashMap<>();

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
                // assistant grabs items from box
                // assistant exits box

                // assistant enters first section
                // assistant stocks
                // assistant exits first section

                // repeat until assistant is empty handed
            }
        }
    }


    // Main class with the main method
    public class Main {
        public static void main(String[] args) {
            // initialize customer threads
            for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
                Thread customer = new Thread(new Customer());
                customer.id = i;

                customer.start();
            }

            // initalize assistant thread
            Thread assistant = new Thread(new Assistant());
            assistant.start();

            ThriftStore thriftStore = new ThriftStore();

            // Start the simulation
            thriftStore.simulate();
        }
    }

}