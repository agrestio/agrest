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
@Table (name = "e23")
public class E23  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "exposedId")
    protected Integer exposedId;

    @Column(name = "name")
    protected String name;


    @OneToMany
    protected List<E26> e26s = new java.util.ArrayList<>();

    public void setE26s(List<E26> e26s) {
        this.e26s = e26s;
    }

    public List<E26> getE26s() {
        return e26s;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExposedId() {
        return exposedId;
    }

    public void setExposedId(Integer exposedId) {
        this.exposedId = exposedId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
