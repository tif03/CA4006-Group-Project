import java.util.concurrent.*;
import java.util.Random;
import java.util.List;
import java.util.Map.Entry;

// TODO figure out all the print statements

public class ThriftStore {
    private static int ticks = 1; // clock

    private static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    private static final int SECTION_NUM = 6;

    private static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    private static final int TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
    private static final int AVERAGE_DELIVERY_INTERVAL = 100;
    private static final int AVERAGE_PURCHASE_INTERVAL = 10;
    private static final int MAX_ITEMS_PER_DELIVERY = 10;
    private static final int MAX_ITEMS_ASSISTANT_CARRY = 10;

    private static final int NUM_CUSTOMERS = 6;

    private Random random = new Random();


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    
    // Define global variables to keep track of sections and items
    private static Box box = new Box(SECTION_NAMES);
    private static Store store = new Store(new ArrayList<>());
    
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

    // class defining thrift store
    public class Store {
        private List<Section> sections = new ArrayList<Section>(); // list of sections

        Store(){
            for (String section : SECTION_NAMES){
                Section sect = new Section(section, 5); // initialize every section to 5 items
                this.sections.add(sect); 
            }
        }

        public Section getSection(String sect_name){
            for (Section sect : this.sections){
                if (sect.getSectionName.equals(sect_name)){
                    return sect;
                }
            }
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //method to make delivery -- it only populates the box
    public void delivery(){
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
            String section = SECTION_NAMES[random.nextInt(SECTION_NUM)];
            box.addItem(section, items); // putting items into the box
        }
    }

    // TODO figure out if this actually works
    // in charge of ONLY incrementing ticks, runs in background
    public void simulate() {
        while (ticks <= TOTAL_TICKS_PER_DAY) { // terminate after a day

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

    public Section findSection(String sect_name){
        for (Section section : store){
            if (section.getSectionName.equals(sect_name)){
                return section;
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // TODO
    class Customer implements Runnable {
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

                // customer enters a section
                int start_time = ticks;
                section.enterSect();

                // purchase is triggered -- if empty it does nothing, if has items it makes purchase
                // decrement num items in section
                if (section.num_items > 0) {
                    Thread.sleep(TICK_DURATION_MILLISECONDS); // customer takes 1 tick to grab item
                    section.num_items--;
                    int finish_time = ticks;
                    System.out.println("<" + ticks + ">" + "<" + Thread.currentThread().getId() + "> Customer = " + this.id  + " Collected_from_section : " + sectionVisit + "Waited_ticks : " + (finish_time - start_time));
                }
                
                // customer exits
                section.exitSect();
            }
        }
    } 

    class Assistant implements Runnable {
        private static ConcurrentHashMap<String, Integer> assistant_inventory = new ConcurrentHashMap<>();

        private long threadID = Thread.currentThread().getId();

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

                // assistant walks over to sections
                Thread.sleep(10 * TICK_DURATION_MILLISECONDS + MAX_ITEMS_ASSISTANT_CARRY * TICK_DURATION_MILLISECONDS);

                for (Entry<String, Integer> entry : assistant_inventory.entrySet()){
                    String inv_section_name = entry.getKey();
                    int inv_num_items = entry.getValue();

                    if (inv_num_items > 0) {

                        Section stock_sect = store.getSection(inv_section_name);
                    
                        // assistant enters section
                        stock_sect.enterSect();

                        stock_sect.num_items += inv_num_items;

                        assistant_inventory.put(inv_section_name, 0); // clear the assistant's inventory for this section

                        stock_sect.exitSect();

                    }
                }
            }
        }
    }

    class Delivery implements Runnable {
        private long getId = Thread.currentThread().getId();

        public void run() {
            while (true) {
                if (random.nextDouble() < 1.0 / AVERAGE_DELIVERY_INTERVAL) {
                    try {
                        delivery();

                        System.out.print("<" + ticks + ">" + "<" + Thread.currentThread().getId() + ">" + "Deposit_of_items : ");
                        for (Entry<String, Integer> item : box.items.entrySet()){
                            String section = entry.getKey();
                            int num_items = entry.getValue();
                            if (num_items > 0){
                                System.out.print(section + " = " + num_items + ", "); 
                            }
                        }
                        System.out.println();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    // Main class with the main method
    public class Main {
        public static void main(String[] args) {

            // Main Delivery thread
            Thread deliveryThread = new Thread(new Delivery());
            deliveryThread.start();

            // initalize assistant thread
            Thread assistant = new Thread(new Assistant());
            assistant.start();

            // initialize customer threads
            for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
                Thread customer = new Thread(new Customer());
                customer.id = i;

                customer.start();
            }

            ThriftStore thriftStore = new ThriftStore();

            // Start the simulation + clock
            thriftStore.simulate(); 
        }
    }

}