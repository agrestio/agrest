package io.agrest.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "e2")
public  class E2  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected String address;

    protected String name;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "e2_id")
    protected List<E3> e3s = new java.util.ArrayList<>();

    public Integer getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<E3> getE3s() {
        return e3s;
    }

    public void setE3s(List<E3> e3s) {
        this.e3s = e3s;
    }
}
