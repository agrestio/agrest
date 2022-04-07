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
@Table (name = "e29")
public  class E29  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id1")
    private Long id1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id2")
    private Long id2;

    @Column (name = "id2Prop")
    protected Integer id2Prop;

    @OneToMany
    protected List<E30> e30s = new java.util.ArrayList<>();


    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public Integer getId2Prop() {
        return id2Prop;
    }

    public void setId2Prop(Integer id2Prop) {
        this.id2Prop = id2Prop;
    }

    public List<E30> getE30s() {
        return e30s;
    }

    public void setE30s(List<E30> e30s) {
        this.e30s = e30s;
    }
}
