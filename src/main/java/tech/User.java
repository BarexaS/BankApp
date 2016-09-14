package tech;

import javax.persistence.*;
import java.util.Map;
import java.util.TreeMap;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue(value = "basic")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true)
    private String login;

    public User() {
    }

    public Map<String, Bill> getBillMap() {
        return billMap;
    }

    public User(String login) {
        this.login = login;
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Map<String, Bill> billMap = new TreeMap<>();

    public void addBill(Bill bill){
        billMap.put(bill.getBillNumber(),bill);
    }
}
