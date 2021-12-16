package base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class BarberShop {
    private static int numberOfFreeSeats;
    private static Semaphore barberSem;
    private static Semaphore customerSem;
    private static Semaphore accessSeatsSem;
    private static boolean flag;
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
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
                System.out.println(dateFormat.format(new Date())+" # "+getName()+" пытается занять место в очереди");
                if(numberOfFreeSeats > 0) {
                    numberOfFreeSeats--; // декремент
                    System.out.println(dateFormat.format(new Date())+" # "+getName()+" занимает очередь");
                    System.out.println(dateFormat.format(new Date())+" # "+"Кол-во мест:" + numberOfFreeSeats);
                    customerSem.release(); // будим парикмахера
                    Thread.sleep(1000);
                    accessSeatsSem.release(); // отдаем мьютекс доступа к посадке на место ожидания
                    barberSem.acquire(); // забираем семафор парикмахера
                    System.out.println(dateFormat.format(new Date())+" # "+getName()+" стрижется");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    barberSem.release(); // парикмахер снова ждет клиента для подстрижки
                    System.out.println(dateFormat.format(new Date())+" # "+getName() + " постригся");
                } else {
                    accessSeatsSem.release(); // отдаем мьютекс
                    System.out.println(dateFormat.format(new Date())+" # "+"К сожалению мест нет и " + getName() + " уходит");
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
            try {
                while(flag && barberSem.availablePermits() == 1) {
                    customerSem.acquire(); // Спит пока не придет customer
                    accessSeatsSem.acquire(); //wait мьютекса для состояния посадки
                    System.out.println(dateFormat.format(new Date())+" # "+"Освобождается место");
                    System.out.println(dateFormat.format(new Date())+" # "+"Кол-во мест после:" + numberOfFreeSeats);
                    numberOfFreeSeats++;
                    accessSeatsSem.release(); // отдать мьютекс доступа к посадке;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        flag = true;
        BarberShop shop = new BarberShop();
        int MAX_COUNT_SEATS = 6;
        numberOfFreeSeats = MAX_COUNT_SEATS;
        customerSem = new Semaphore(MAX_COUNT_SEATS,true);
        barberSem = new Semaphore(1,true);
        accessSeatsSem = new Semaphore(1,true);//mutex
        customerSem.acquire(6);
        BarberShop.Barber barber = shop.new Barber();
        Thread barberTH = new Thread(barber);
        barberTH.start();
        Thread.sleep(1000);
        for(int i=1;i!=6;i++){
            Thread customerTH = new Thread(shop.new Customer("Клиент №" + i));
            customerTH.start();
            Thread.sleep(500);
        }
        Thread.sleep(20000);
        System.out.println(dateFormat.format(new Date())+" # Парикмахерская закрывается");
        System.exit(0);
    }
}
