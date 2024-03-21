package store;
import java.util.*;
import java.util.Random;


class Customer implements Runnable {

    public static Random random = new Random();
    private final int id;
    // public long getId = Thread.currentThread().getId();

    public Customer(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        while (true) {
            // choose random section to visit
            String sectionVisit = thriftstore.SECTION_NAMES[random.nextInt(thriftstore.SECTION_NAMES.length)];

            // TODO enter section

            if (thriftstore.thrift_store.get(sectionVisit) > 0){
                int temp = thriftstore.thrift_store.get(sectionVisit);
                thriftstore.thrift_store.put(sectionVisit, temp - 1);
            }

            // TODO print
            // TODO exit section
        }
    }
} 