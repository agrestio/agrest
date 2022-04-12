package io.agrest.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "e5")
public class E5 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected Date date;
    protected String name;

    @OneToMany
    protected List<E15> e15s = new java.util.ArrayList<>();

    @OneToMany
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
