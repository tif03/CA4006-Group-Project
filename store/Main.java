package store;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class Main {

    public static AtomicInteger ticks;
    public static final int TOTAL_TICKS_PER_DAY = 1000;    // 1000 ticks per day
    public static final long TICK_DURATION_MILLISECONDS = 100; // 100 Milliseconds per tick
    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};

    public static final int NUM_CUSTOMERS = 6;


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
                thriftstore.delivery();
            }
            
        }
    }

    public static void main(String[] arg){

        Map<String, Integer> init_store = new HashMap<>();

        for (String section_name : SECTION_NAMES) {
            init_store.put(section_name, 5);
        } 

        thriftstore store = new thriftstore(init_store);

        // set clock to 0
        ticks = new AtomicInteger(0); 

        Thread assistant = new Thread(new Assistant());
        assistant.start();

        List<Thread> customer_threads = new ArrayList<>();

        // initialize customer threads
        for (int i = 1; i < NUM_CUSTOMERS + 1; i++) {
            Thread customer = new Thread(new Customer(i));
            customer_threads.add(customer);
            customer.start();
        }

        simulate();
    }
}
