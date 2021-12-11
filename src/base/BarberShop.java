package base;

import java.util.concurrent.Semaphore;

public class BarberShop {

    public static void main(String[] args) {
        System.out.println("hello," + args[0]);
        int MAX_COUNT_SITS = 6;
        Semaphore customers_wait_semaphore = new Semaphore(MAX_COUNT_SITS,true);
        Semaphore barber_semaphore = new Semaphore(1,true);
        Semaphore accessSeats = new Semaphore(1,true);//mutex
        int count_customers;
    }
}
