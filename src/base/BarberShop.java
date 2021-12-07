package base;

import sun.awt.Mutex;
import java.util.concurrent.Semaphore;

public class BarberShop {

    public static void main(String[] args) {
        System.out.println("hello," + args[0]);
        int MAX_COUNT = 6;
        Semaphore customers_wait_semaphore = new Semaphore(MAX_COUNT,true);
        Semaphore barber_semaphore = new Semaphore(1,true);
        Mutex sit_on_wait_chair = new Mutex();//synchronized ()

    }
}
