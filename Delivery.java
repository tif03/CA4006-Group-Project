class Delivery implements Runnable {
    private long getId = Thread.currentThread().getId();

    public void run() {
        while (true) {
            if (random.nextDouble() < 1.0 / AVERAGE_DELIVERY_INTERVAL) {
                try {
                    delivery();

                    System.out.print("<" + ticks + ">" + "<" + Thread.currentThread().getId() + ">" + "Deposit_of_items : ");
                    for (Entry<String, Integer> item : box.items.entrySet()){
                        String section = item.getKey();
                        int num_items = item.getValue();
                        if (num_items > 0){
                            System.out.print(section + " = " + num_items + ", "); 
                        }
                    }
                    System.out.println();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}