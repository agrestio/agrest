package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "e8")
public class E8 {
    public static final String E7S = "e7s";
    public static final String E9 = "e9";

    @Id
    private Integer id;

    protected String name;

    @OneToMany(mappedBy = "e8")
    protected List<E7> e7s = new java.util.ArrayList<>();

    @OneToOne(mappedBy = "e8")
    protected E9 e9;


    public void setE7s(List<E7> e7s) {
        this.e7s = e7s;
    }

    public List<E7> getE7s() {
        return e7s;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public E9 getE9() {
        return e9;
    }

    public void setE9(E9 e9) {
        this.e9 = e9;
    }
}
