class Assistant implements Runnable {
    private ConcurrentHashMap<String, Integer> assistant_inventory = new ConcurrentHashMap<>();

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
