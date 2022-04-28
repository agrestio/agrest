package io.agrest.jpa.model;

import jakarta.persistence.*;


import java.util.List;

@Entity
@Table (name = "e12")
public  class E12  {
    public static final String E1213 = "e1213";

    @Id
    private Integer id;

    @OneToMany(mappedBy = "e12",cascade = CascadeType.REMOVE)
    protected List<E12E13> e12E13s = new java.util.ArrayList<>();

    public void setE12E13s(List<E12E13> e12E13s) {
        this.e12E13s = e12E13s;
    }

    public List<E12E13> getE12E13s() {
        return e12E13s;
    }

}
