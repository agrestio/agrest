package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table (name = "e8")
public  class E8  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String name;

    @OneToMany
    @JoinColumn (name = "e8_id")
    protected List<E7> e7s = new java.util.ArrayList<>();

    @OneToOne
    protected E9 e9;

    public void setE7s(List<E7> e7s) {
        this.e7s = e7s;
    }

    public List<E7> getE7s() {
        return e7s;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
