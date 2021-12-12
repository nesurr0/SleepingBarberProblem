package base;

import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class BarberShop {
    private static int numberOfFreeSeats;
    private static Semaphore barberSem;
    private static Semaphore customerSem;
    private static Semaphore accessSeatsSem;
    private static PriorityQueue<String> names;
    private static boolean flag;

    public class Customer implements Runnable{

        final private String name;
        Customer(String name){
            this.name = name;
        }
        Customer(){
            name = "Парикмахер";
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                accessSeatsSem.acquire(); // Только один поток имеет доступ
                if(numberOfFreeSeats > 0) {
                    numberOfFreeSeats--; // декремент
                    customerSem.release(); // будим парикмахера
                    accessSeatsSem.release(); // освобождаем место
                    numberOfFreeSeats++;
                    accessSeatsSem.release(); // отдаем мьютекс доступа к посадке
                    barberSem.acquire(); // ждем открытия семафора ждем стрижки
                    // стригут клиента
                } else {
                    accessSeatsSem.release(); // отдаем мьютекс
                    // уходим
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //////////////////////////////////////////////////////////////////////

    public class Barber implements Runnable{

        final private String name;
        Barber(String name){
            this.name = name;
        }

        Barber(){
            name = "Парикмахер";
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            while(flag) {
                try {
                    customerSem.acquire(); // Спит пока не придет customer
                    accessSeatsSem.acquire(); //wait Одноместного семафора для блокировки изменения состояния доступных клиентов
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                barberSem.release(); // ждать семафор клиента для подстрижки
                accessSeatsSem.release(); // отдать мьютекс доступа к посадке
                System.out.println("Парикмахер стрижет -> " + names.remove());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        flag = true;
        BarberShop shop = new BarberShop();
        int MAX_COUNT_SITS = 6;
        numberOfFreeSeats = MAX_COUNT_SITS;
        customerSem = new Semaphore(MAX_COUNT_SITS,true);
        barberSem = new Semaphore(1,true);
        accessSeatsSem = new Semaphore(1,true);//mutex
        customerSem.acquire(MAX_COUNT_SITS);
        BarberShop.Barber barber = shop.new Barber();
        Thread barberTH = new Thread(barber);
        barberTH.start();
        names = new PriorityQueue<String>(6);
        for(int i=1;i!=7;i++){
            Thread customerTH = new Thread(shop.new Customer("Клиент №" + i));
            customerTH.start();
            names.add("Клиент №" + i);
        }
    }
}
