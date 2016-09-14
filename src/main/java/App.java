import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.*;
import tech.currency.JSON;
import tech.currency.Rate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;


/*Создать базу данных «Банк» с таблицами «Пользователи», «Транзакции», «Счета» и «Курсы валют».
       + Счет бывает 3-х видов: USD, EUR, UAH.
        + Написать запросы для пополнения счета в нужной валюте,
        + перевода средств с одного счета на другой,
        + конвертации валюты по курсу в рамках счетов одного пользователя.
        Написать запрос для получения суммарных средств на счету одного пользователя в UAH (расчет по курсу).*/
public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATest");
        EntityManager em = emf.createEntityManager();

        initDB(em);

        GetThread th = new GetThread(em);
        th.setDaemon(true);
        th.start();

        try {
            while (true) {
                System.out.println("Input for action:");
                System.out.println("1 - add money");
                System.out.println("2 - Transfer money to another bill");
                System.out.println("3 - Overall Balance");
                String param = sc.nextLine();
                switch (param) {
                    case "1":
                        addMoney(em, sc);
                        break;
                    case "2":
                        transferMoney(em, sc);
                        break;
                    case "3":
                        overallBalance(em, sc);
                        break;
                    default:
                        System.out.println("Wrong parameter!");
                        return;
                }
            }
        } finally {
            sc.close();
            em.close();
            emf.close();
        }
    }

    private static void overallBalance(EntityManager em, Scanner sc) {
        String request = "SELECT u FROM User u WHERE u.login = :login";
        System.out.println("Input user login");
        String login = sc.nextLine();
        Query query = em.createQuery(request, User.class);
        query.setParameter("login", login);
        User user = (User) query.getSingleResult();
        Double sum = 0.0;
        for (Map.Entry<String, Bill> entry : user.getBillMap().entrySet()) {
            Currency curren = em.find(Currency.class, entry.getValue().getCurrency().toString());
            sum = sum + entry.getValue().getBalance() / curren.getRatio();
        }
        System.out.println("Your current overall balance - " + sum + " UAH");
    }

    private static void transferMoney(EntityManager em, Scanner sc) {
        System.out.println("Enter your bill number");
        String fromBillNumber = sc.nextLine();
        Bill from = getBillByNumber(em, fromBillNumber);
        System.out.println("Currency of your bill is - " + from.getCurrency());

        System.out.println("Enter recipient bill number");
        String toBillNumber = sc.nextLine();
        Bill to = getBillByNumber(em, toBillNumber);
        System.out.println("Currency of your bill is - " + to.getCurrency());

        if (!from.getCurrency().toString().equals(to.getCurrency().toString())) {
            System.out.println("Currencies does not match.");
            System.out.println("Bank will make auto-change.");
        }
        newTransAct(sc, from, to, em);
        System.out.println("Your current balance - " + from.getBalance() + " " + from.getCurrency());

    }

    private static void addMoney(EntityManager em, Scanner sc) {


        System.out.println("Enter bill number");
        String billNumber = sc.nextLine();
        Bill currBill = getBillByNumber(em, billNumber);

        System.out.println("Bill currency is - " + currBill.getCurrency());
        newTransAct(sc, null, currBill, em);
        System.out.println("Your current balance - " + currBill.getBalance() + " " + currBill.getCurrency());
    }

    private static void newTransAct(Scanner sc, Bill from, Bill to, EntityManager em) {
        boolean status = false;
        while (status == false) {
            System.out.println("Enter input currency");
            String curr = sc.nextLine();
            System.out.println("Enter amount of money");
            Double amount = Double.parseDouble(sc.nextLine());
            if (amount <= 0) {
                System.out.println("Wrong amount input");
            } else {
                to.newTrans(from, to, curr, amount, em);
                status = true;
            }
        }
//        if (from != null )em.persist(from);
//        if (to != null )em.persist(to);
        System.out.println("Transaction completed.");
    }

    private static Bill getBillByNumber(EntityManager em, String billNumber) {
        Query query = em.createQuery("SELECT b FROM Bill b WHERE b.billNumber = :billNumber");
        query.setParameter("billNumber", billNumber);
        return (Bill) query.getSingleResult();
    }

    private static void initDB(EntityManager em) {
        em.getTransaction().begin();
        try {
            User user1 = new User("login1");
            User user2 = new User("login2");

            user1.addBill(new BillUAH("000001", user1));
            user1.addBill(new BillEUR("000002", user1));
            user1.addBill(new BillUSD("000003", user1));

            user2.addBill(new BillUAH("000004", user2));
            user2.addBill(new BillEUR("000005", user2));
            user2.addBill(new BillUSD("000006", user2));

            em.persist(user1);
            em.persist(user2);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }
}

class GetThread extends Thread {
    EntityManager em;

    public GetThread(EntityManager em) {
        this.em = em;
    }

    @Override
    public void run() {
        try {
            initCurrency();
            updateCurrencyRatio();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private void initCurrency() {
        em.getTransaction().begin();
        try {
            Currency eur = new Currency("EUR", 0.0);
            Currency usd = new Currency("USD", 0.0);
            Currency uah = new Currency("UAH", 0.0);
            em.persist(eur);
            em.persist(usd);
            em.persist(uah);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    private void updateCurrencyRatio() throws IOException {
        String request = "http://query.yahooapis.com/v1/public/yql?format=json&q=select%20*%20from%20" +
                "yahoo.finance.xchange%20where%20pair%20in%20(\"UAHEUR\",%20\"UAHUSD\",%20\"UAHUAH\")&env=store://datatables.org/alltableswithkeys";

        String result = performRequest(request);

        Gson gson = new GsonBuilder().create();
        JSON json = gson.fromJson(result, JSON.class);

        for (Rate rate : json.query.results.rate) {
            Currency eur = em.find(Currency.class, "EUR");
            Currency usd = em.find(Currency.class, "USD");
            Currency uah = em.find(Currency.class, "UAH");
            em.getTransaction().begin();
            try {
                if (rate.id.equalsIgnoreCase("UAHEUR")) eur.setRatio(rate.Rate);
                if (rate.id.equalsIgnoreCase("UAHUSD")) usd.setRatio(rate.Rate);
                if (rate.id.equalsIgnoreCase("UAHUAH")) uah.setRatio(rate.Rate);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
            }
        }
    }

    private static String performRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        StringBuilder sb = new StringBuilder();

        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            char[] buf = new char[1000000];

            int r = 0;
            do {
                if ((r = br.read(buf)) > 0)
                    sb.append(new String(buf, 0, r));
            } while (r > 0);
        } finally {
            http.disconnect();
        }

        return sb.toString();
    }

}
