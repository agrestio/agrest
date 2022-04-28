package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "e5")
public class E5 {
    public static final String E15S = "e15s";

    @Id
    private Integer id;

    protected Date date;
    protected String name;


    @ManyToMany
    @JoinTable(name = "e15_e5",
            joinColumns = @JoinColumn(name = "e15_id"),
            inverseJoinColumns = @JoinColumn(name = "e5_id"))
    protected List<E15> e15s = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "e5",cascade = {jakarta.persistence.CascadeType.REMOVE})
    protected List<E3> e3s = new java.util.ArrayList<>();

    public void setE15s(List<E15> e15s) {
        this.e15s = e15s;
    }

    public List<E15> getE15s() {
        return e15s;
    }

    public void setE3s(List<E3> e3s) {
        this.e3s = e3s;
    }

    public List<E3> getE3s() {
        return e3s;
    }

    public Integer getId() {
        return id;
    }


}
