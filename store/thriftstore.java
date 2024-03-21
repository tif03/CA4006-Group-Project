package store;
import java.util.concurrent.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class thriftstore {

    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    public static Map<String, Integer> delivery_box = new Hashtable<>();
    public static Map<String, Integer> thrift_store = new Hashtable<>();

    public static Boolean delivery_made = false;

    public static final int NUM_CUSTOMERS = 6;
    public static AtomicInteger ticks;
    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick

    public static final int MAX_ITEMS_PER_DELIVERY = 10;

    public static Random random = new Random();

    thriftstore (Map<String, Integer> store){
        thrift_store = store;
    }

    public static synchronized void delivery() {
            int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
            while (remain > 0){

                String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
                int items = random.nextInt(remain + 1);
                delivery_box.put(section, delivery_box.getOrDefault(section, 0) + 1); // putting items into the box
                
                remain -= items;
            }

            // put remaining items into a random section
            // if (remain > 0){
            //     String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.length)];
            //     int temp = delivery_box.get(section);
            //     delivery_box.put(section, temp + remain); // putting items into the box
            // }

            delivery_made = true;
            System.out.println("<" + Main.ticks.get() + ">" + "<Thread_ID>" + "Deposit_of_items : ");
            for (Map.Entry<String, Integer> entry : ((Hashtable<String, Integer>) delivery_box).entrySet()) {
                String item = entry.getKey();
                int quantity = entry.getValue();
                System.out.print(item + " = " + quantity + " ");
            } 
            System.out.println();
        }
    
}