import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

class Assistant implements Runnable {

    public static Random random = new Random();

    private Dictionary<String, Integer> assistant_inventory = new Hashtable<>();

    private long getId = Thread.currentThread().getId();
    private int MAX_ITEMS_ASSISTANT_CARRY = 10;

    // initialize the assistant -- inventory is 0
    public Assistant() {
        for (String section : thriftstore.SECTION_NAMES) {
            assistant_inventory.put(section, 0);
        }
    }


@Override
public void run() {
    while (true) {

        // assistant grabs 10 random items from box
        // TODO figure out a semaphore for the box
        if (thriftstore.delivery_made) {

            thriftstore.delivery_made = false; // once an assistant accounts for a delivery back to false until next one

            for (int items = 0; items < MAX_ITEMS_ASSISTANT_CARRY; items++) {
                String randomSection = thriftstore.SECTION_NAMES[random.nextInt(thriftstore.SECTION_NAMES.length)];

                int temp = assistant_inventory.get(randomSection); // previous inventory value

                if (thriftstore.delivery_box.get(randomSection) > 0) {
                    thriftstore.delivery_box.put(randomSection, temp - 1);
                    assistant_inventory.put(randomSection, temp + 1); // increment
                } else {
                    items--;
                }
            }
        }

        // TODO ticks and stuff

        int remain = MAX_ITEMS_ASSISTANT_CARRY; 
        for (Entry<String, Integer> entry : ((Hashtable<String, Integer>) assistant_inventory).entrySet()){
            String inv_section_name = entry.getKey();
            int inv_num_items = entry.getValue();

            // TODO enter section

            if (inv_num_items > 0) {

                int temp = thriftstore.thrift_store.get(inv_section_name);

                // assistant stocks the thrift store shelves with items in inventory
                thriftstore.thrift_store.put(inv_section_name, temp + inv_num_items); 

                assistant_inventory.put(inv_section_name, 0); // clear the assistant's inventory for this section

            }

            // TODO exit section
            // TODO print
        }
    }
}
}


