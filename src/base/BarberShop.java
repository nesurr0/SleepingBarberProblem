package base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class BarberShop {
    private static int numberOfFreeSeats;
    private static Semaphore barberSem;
    private static Semaphore customerSem;
    private static Semaphore accessSeatsSem;
    private static boolean flag;
    private static final DateFormat dateFormat = new SimpleDateFormat("mm:ss");
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
                //////
                Thread.sleep(5000);
                //////
                if(numberOfFreeSeats > 0) {
                    numberOfFreeSeats--; // декремент
                    System.out.println(dateFormat.format(new Date())+" # "+getName()+" занимает очередь");
                    System.out.println(dateFormat.format(new Date())+" # "+"Кол-во мест:" + numberOfFreeSeats);
                    accessSeatsSem.release(); // отдаем мьютекс доступа к посадке на место ожидания
                    barberSem.acquire(); // забираем семафор парикмахера
                    customerSem.release(); // будим парикмахера
                    accessSeatsSem.acquire(); //wait мьютекса для состояния посадки
                    numberOfFreeSeats++;
                    System.out.println(dateFormat.format(new Date())+" # "+"Освободилось место в комнате ожидания:"+numberOfFreeSeats);
                    accessSeatsSem.release(); // отдать мьютекс доступа к посадке;
                    System.out.println(dateFormat.format(new Date())+" # "+getName()+" стрижется");
                    Thread.sleep(15000);//(new Random().nextInt(20000))
                    System.out.println(dateFormat.format(new Date())+" # "+getName() + " постригся");
                    System.out.println(dateFormat.format(new Date())+" # "+"Освобождается кресло у парикмахера");
                    barberSem.release(); // парикмахер снова ждет клиента для подстрижки
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
            boolean sleep=false;
            while(flag){
                while(barberSem.availablePermits() == 1) {
                    try {
                    Thread.sleep(3000);
                    if(customerSem.availablePermits() == 0){
                        System.out.println(dateFormat.format(new Date())+" # "+"Парикмахер спит");
                        sleep = true;
                    }
                    customerSem.acquire();
                    if(sleep){
                        sleep = false;
                        System.out.println(dateFormat.format(new Date())+" # "+"Парикмахер проснулся");
                    }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
        customerSem.acquire(MAX_COUNT_SEATS);

        new Thread(shop.new Barber()).start();
        Thread.sleep(5000);
        for(int i=1;i!=6;i++){
            new Thread(shop.new Customer("Клиент №" + i)).start();
            Thread.sleep(500);
        }
        Thread.sleep(100000);
        System.out.println(dateFormat.format(new Date())+" # Парикмахерская закрывается");
        System.exit(0);

    }
}
