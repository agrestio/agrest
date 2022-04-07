package io.agrest.jpa.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;


@Entity
@Table (name = "e17")
public  class E17 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id1", nullable = false)
    protected Integer id1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id2")
    protected Integer id2;

    @Column(name = "name")
    protected String name;

    @OneToMany
    protected List<E18> e18s = new java.util.ArrayList<>();

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setE18s(List<E18> e18s) {
        this.e18s = e18s;
    }

    public List<E18> getE18s() {
        return e18s;
    }

    public String getName() {
        return name;
    }


}
