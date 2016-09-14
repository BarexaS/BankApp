package tech;

import javax.persistence.*;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_from")
    private Bill from;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_to")
    private Bill to;
    private String currency;

    private Double amount;

    public Transaction(Bill from, Bill to, String currency, Double amount) {
        this.from = from;
        this.to = to;
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {

        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Transaction() {
    }


    public Bill getFrom() {
        return from;
    }

    public void setFrom(Bill from) {
        this.from = from;
    }

    public Bill getTo() {
        return to;
    }

    public void setTo(Bill to) {
        this.to = to;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
