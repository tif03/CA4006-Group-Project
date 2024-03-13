import java.util.concurrent.*;
import java.util.Random;

public class ThriftStore{

    private static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};
    private static final int TOTAL_TICKS_PER_DAY = 1000;    //1000 ticks per day
    private static final int TICK_DURATION_MILLISECONDS = 100; // Milliseconds per tick
    private static final int AVERAGE_DELIVERY_INTERVAL = 100;
    private static final int MAX_ITEMS_PER_DELIVERY = 10;

    // Define variables to keep track of sections and items
    private static ConcurrentHashMap<String, Integer> box = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>(); 

    // Semaphores
    private static Semaphore mutex_box = new Semaphore(1);
    private static ConcurrentHashMap<String, Semaphore> sect_sem = new ConcurrentHashMap<>();

    public static void main(String[] args){
        //initialize inventory
        for (String section : SECTION_NAMES) {
            inventory.put(section, 5);   // each section begins with 5 items
            sect_sem.put(section, new Semaphore(1));
        }

        //create and start assistant thread
        Thread assistant = new Thread(new Assistant());
        assistant.start();

        //create and start customer threads -- 6 customer threads
        for (int i = 0; i < 5; i++) {
            Thread customer = new Thread(new Customer());
            customer.start();
        }

        //start simulation
        simulate();
    }

    //method to simulate thrift store
    private static void simulate(){
        // seed the random generator
        Random randgen = new Random(123);

        // wait time between deliveries
        int waitTicks = (int) (2 * randgen.nextDouble() * 100);

        // tick counter
        int tick = 1;

        // main loop -- run for a day
        while (tick <= TOTAL_TICKS_PER_DAY){
            if (waitTicks <= 0){    // once wait time is over
                makeDelivery(tick); // call delivery function
                waitTicks = (int) (2 * randgen.nextDouble() * 100); // generate new wait time
            }

            // decrease wait tick + increase tick
            waitTicks--;
            tick++;

            // sleep for a tick + introduce delay to create more accurate results
            try {
                Thread.sleep(TICK_DURATION_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //method to make delivery
    private static void delivery(){
        // loop to simulate delivery

        int remain = MAX_ITEMS_PER_DELIVERY;    // remaining space out of 10
        int section_index = 0;
        while (remain >= 0 && section_index <= SECTION_NAMES.size()){
            int items = random.nextInt(remain + 1); // generate random number 0 to remaining space
            String section = SECTION_NAMES[section_index];
            inventory.put(section, inventory.get(section) + items);
            remain -= items;

            // put the items in the box
            box.put(section, box.get(section) + items);

            section_index += 1;
        }

        // put remaining items into a random section
        if (remain > 0){
            String section = SECTION_NAMES[random.nextInt(SECTION_NAMES.size())];
            inventory.put(section, inventory.get(section) + remain);

            box.put(section, box.get(section) + items);
        }

        System.out.print("<" + tick + "> <Assistant> Deposit_of_itmes : ");
        for (Entry<String, Integer> item : inventory.entrySet()){
            String section = entry.getKey();
            int num_items = entry.getValue();
            System.out.print(section + " = " + num_items + ", "); 
        }

    }

    // Assistant threads
    class Assistant implements Runnable{
        @Override
        public void run(){
            //wait for box
            //grabs delivery items from box
            //signal box for next
            //goes to sections
            //wait for section
            //put stuff down
            //signal section
        }
    }

    // Customer threads
    class Customer implements Runnable{
        //wait for section (either for it to be empty or for it to be stocked)
        //purchases
        //signal section
    }

}