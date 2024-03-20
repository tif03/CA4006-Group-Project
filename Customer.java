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
            section.enterSect();

            // purchase is triggered -- if empty it does nothing, if has items it makes purchase
            // decrement num items in section
            if (section.num_items > 0) {
                Thread.sleep(TICK_DURATION_MILLISECONDS); // customer takes 1 tick to grab item
                section.num_items--;
                System.out.println("<" + ticks + ">" + "<" + Thread.currentThread().getId() + "> Customer = " + this.id  + " Collected_from_section : " + sectionVisit + "Waited_ticks : " + (finish_time - start_time));
            }
            
            // customer exits
            section.exitSect();
        }
    }
} 