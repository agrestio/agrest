package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "e13")
public  class E13  {

    @Id
    private Integer id;

    @OneToMany(mappedBy = "e13",cascade = CascadeType.REMOVE)
    protected List<E12E13> e12E13s = new java.util.ArrayList<>();

    public List<E12E13> getE12E13s() {
        return e12E13s;
    }


}
