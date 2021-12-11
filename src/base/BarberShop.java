package base;

import java.util.concurrent.Semaphore;

public class BarberShop {
    private int numberOfFreeSeats;
    private Semaphore barberSem;
    private Semaphore customerSem;
    private Semaphore accessSeatsSem;
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
            try {
                customerSem.acquire(); // Спит пока не придет customer
                accessSeatsSem.acquire(); //wait Одноместного семафора для блокировки изменения состояния доступных клиентов
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            barberSem.release(); // ждать семафор клиента для подстрижки
            accessSeatsSem.release(); // отдать мьютекс доступа к посадке
            //System.out.println(Парикмахер стрижет клиента);
        }
    }



    public static void main(String[] args) {
        int MAX_COUNT_SITS = 6;
        Semaphore customers_semaphore = new Semaphore(MAX_COUNT_SITS,true);
        Semaphore barber_semaphore = new Semaphore(1,true);
        Semaphore accessSeats_semaphore = new Semaphore(1,true);//mutex
        int count_customers;
        int numberOfFreeSeats;
    }
}
