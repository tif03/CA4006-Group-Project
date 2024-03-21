import java.util.concurrent.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class thriftstore {

    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    public static Dictionary<String, Integer> delivery_box = new Hashtable<>();
    public static Dictionary<String, Integer> thrift_store = new Hashtable<>();

    public static Boolean delivery_made = false;

    public static final int NUM_CUSTOMERS = 6;
    public static AtomicInteger ticks;
    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick


    public static final int MAX_ITEMS_PER_DELIVERY = 10;

    public static Random random = new Random();

    public static synchronized void delivery() {
        if (ticks.get() % 100 == 0) {
            int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
            int section_index = 0;
            while (remain >= 0 && section_index < SECTION_NAMES.length){
                int items = random.nextInt(remain + 1); // generate random number 0 to remaining space
                String section = SECTION_NAMES[section_index];
                delivery_box.put(section, items); // putting items into the box
                remain -= items;

                section_index += 1;
            }

            // put remaining items into a random section
            if (remain > 0){
                String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
                delivery_box.put(section, remain); // putting items into the box
            }

            delivery_made = true;
            System.out.println("<" + ticks.get() + ">" + "<Thread_ID>" + "Deposit_of_items :");
            for (Map.Entry<String, Integer> entry : ((Hashtable<String, Integer>) delivery_box).entrySet()) {
                String item = entry.getKey();
                int quantity = entry.getValue();
                System.out.print(item + "=" + quantity + "");
            }
        }
    }

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

    public static void main(String[] arg){
        // set clock to 0
        ticks = new AtomicInteger(0); 

        // initialize thrift store
        for (String section_name : SECTION_NAMES) {
            thrift_store.put(section_name, 5);
        } 

        Thread assistant = new Thread(new Assistant());
        assistant.start();

        List<Thread> customer_threads = new ArrayList<>();

        // initialize customer threads
        for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
            Thread customer = new Thread(new Customer(i));
            customer_threads.add(customer);
            customer.start();
        }
    }
    
}