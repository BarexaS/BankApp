package tech;

import javax.persistence.*;

@Entity
@Table(name = "Bills")
@DiscriminatorValue(value = "UAH")
public class BillUAH extends Bill {

    @Override
    public void newTrans(Bill from, Bill to, String curr, Double amount, EntityManager em) {
        Currency curren = em.find(Currency.class,curr);
        //Сумма перевода в грн
        Double temp = amount/curren.getRatio();
        em.getTransaction().begin();
        try {
            try {
                Double amountTo = temp * em.find(Currency.class, to.getCurrency().toString()).getRatio();
                to.addTrans(new Transaction(from, to, curr, amountTo));
                to.setBalance(to.getBalance() + amountTo);
            } catch (NullPointerException ex) {
                //NOP
            }
            try {
                Double amountFrom = temp * em.find(Currency.class, from.getCurrency().toString()).getRatio();
                from.addTrans(new Transaction(to, from, curr, amountFrom));
                from.setBalance(from.getBalance() - amountFrom);
            } catch (NullPointerException ex) {
                //NOP
            }
            em.getTransaction().commit();
        }catch (Exception ex){
            if (em.getTransaction().isActive())
            em.getTransaction().rollback();
        }
    }

    public BillUAH(String billNumber, User owner) {
        super(CurrencyTypes.UAH, billNumber, owner);
    }

    public BillUAH() {
    }
}
