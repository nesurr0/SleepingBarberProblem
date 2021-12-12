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
            name = "Клиент";
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                accessSeatsSem.acquire(); // Только один поток имеет доступ
                System.out.println(getName()+" пытается занять место в очереди");
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(numberOfFreeSeats > 0) {
                    numberOfFreeSeats--; // декремент
                    customerSem.release(); // будим парикмахера
                    accessSeatsSem.release(); // освобождаем место
                    numberOfFreeSeats++;
                    accessSeatsSem.release(); // отдаем мьютекс доступа к посадке
                    barberSem.acquire(); // ждем открытия семафора ждем стрижки
                    System.out.println(getName()+" стрижется");
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    accessSeatsSem.release(); // отдаем мьютекс
                    System.out.println("К сожалению мест нет и" + getName() + "уходит");
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
        names = new PriorityQueue<String>(8);
        for(int i=1;i!=10;i++){
            Thread customerTH = new Thread(shop.new Customer("Клиент №" + i));
            customerTH.start();
            names.add("Клиент №" + i);
        }
    }
}
