package io.agrest.jpa.model;

import jakarta.persistence.Column;
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
    @Column(nullable = false)
    private Integer id;

    @Column
    protected Date date;
    @Column
    protected String name;

    protected Object e15s;
    @OneToMany
    protected List<E3> e3s = new java.util.ArrayList<>();

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
