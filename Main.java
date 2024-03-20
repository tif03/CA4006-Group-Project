import java.util.concurrent.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

// TODO -- figure out how variables translate to respective class
public class Main {
    AtomicInteger ticks = new AtomicInteger(0); // set clock to 0

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

    // Define global variables to keep track of sections and items
    private static Box box;
    private static Store store;


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
            box.addItem(section, remain); // putting items into the box
        }
    }

    // TODO figure out if this actually works
    // in charge of ONLY incrementing ticks, runs in background
    public void simulate() {
        while (ticks.get() <= TOTAL_TICKS_PER_DAY) { // terminate after a day
            // current tick
            ticks.incrementAndGet();

            // always signal assistant to stock items
            // stocking event if box is full

            // code to trigger a customer thread

        }

    }

    public Section findSection(String sect_name){
        for (Section section : store.sections){
            if (section.section_name.equals(sect_name)){
                return section;
            }
        }

        //TODO we have to change this o.o
        return new Section("kill me", 0); 
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void main(String[] args) {

        ThriftStore thriftStore = new ThriftStore();
        box = new Box(SECTION_NAMES);
        store = new Store();

        // Main Delivery thread
        Thread deliveryThread = new Thread(new Delivery());
        deliveryThread.start();

        // initalize assistant thread
        Thread assistant = new Thread(new Assistant());
        assistant.start();

        // initialize customer threads
        for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
            Thread customer = new Thread(new Customer(i));

            customer.start();
        }

        // Start the simulation + clock
        thriftStore.simulate(); 
    }
 
}
