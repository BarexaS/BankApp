package tech;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Bills")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "BillType", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(value = "basic")
public abstract class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Transaction> transList = new ArrayList<>();

    private double balance;
    @Enumerated(EnumType.STRING)
    private CurrencyTypes currency;

    @Column(nullable = false)
    private String billNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    public abstract void newTrans(Bill from, Bill to, String curr, Double amount, EntityManager em);

    public Bill(CurrencyTypes currency, String billNumber, User owner) {
        this.currency = currency;
        this.billNumber = billNumber;
        this.owner = owner;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void addTrans(Transaction trans) {
        transList.add(trans);

    }

    public Bill() {
    }

    public CurrencyTypes getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyTypes currency) {
        this.currency = currency;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public double getBalance() {
       return balance;
    }


    public List<Transaction> getTransList() {
        return Collections.unmodifiableList(transList);
    }

    public User getOwner() {
        return owner;
    }
}
