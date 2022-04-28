package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "e15")
public class E15 {
    public static final String E14S = "e14s";
    public static final String E15E1 = "e15e1";

    @Id
    private Long long_id;

    protected String name;

    @OneToMany(cascade = {jakarta.persistence.CascadeType.REMOVE})
    @JoinColumn (name = "e14s_id")
    protected List<E14> e14s = new java.util.ArrayList<>();

    @OneToMany(cascade = {jakarta.persistence.CascadeType.REMOVE})
    @JoinColumn (name = "e15e1s_id")
    protected List<E15E1> e15E1s = new java.util.ArrayList<>();


    @ManyToMany (mappedBy = "e15s")
    protected List<E5> e5s = new java.util.ArrayList<>();

    public Long getLong_id() {
        return long_id;
    }

    public void setLong_id(Long long_id) {
        this.long_id = long_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<E14> getE14s() {
        return e14s;
    }

    public void setE14s(List<E14> e14s) {
        this.e14s = e14s;
    }

    public List<E15E1> getE15E1s() {
        return e15E1s;
    }

    public void setE15E1s(List<E15E1> e15E1s) {
        this.e15E1s = e15E1s;
    }

    public List<E5> getE5s() {
        return e5s;
    }

    public void setE5s(List<E5> e5s) {
        this.e5s = e5s;
    }
}
