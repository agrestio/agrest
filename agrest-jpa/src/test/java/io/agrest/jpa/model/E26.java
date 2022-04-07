package io.agrest.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table (name = "e26")
public  class E26  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @OneToOne
    protected E23 e23;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public E23 getE23() {
        return e23;
    }

    public void setE23(E23 e23) {
        this.e23 = e23;
    }
}
