package base;

import java.util.concurrent.Semaphore;

public class Barber implements Runnable{
    private Semaphore barber_semaphore;
    private String name;
    Barber(Semaphore sem, String name){
        barber_semaphore = sem;
    }
    Barber(Semaphore sem){
        barber_semaphore = sem;
        name = "Парикмахер";
    }

    public String getName() {
        return name;
    }

    public Semaphore getBarber_semaphore() {
        return barber_semaphore;
    }

    @Override
    public void run() {
        while(true){

        }
    }
}
