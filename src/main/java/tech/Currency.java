package tech;

import javax.persistence.*;

@Entity
@Table(name = "Currency")
public class Currency {

    @Id
    @Column(unique = true)
    private String name;
    private double ratio;

    public Currency(String name, double ratio) {
        this.name = name;
        this.ratio = ratio;
    }

    public Currency() {
    }

    public String getName() {
        return name;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }
}
