package io.agrest.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "e15_e1")
public class E15E1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    protected E1 e1;

    @OneToOne
    protected E15 e15;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public E1 getE1() {
        return e1;
    }

    public void setE1(E1 e1) {
        this.e1 = e1;
    }

    public E15 getE15() {
        return e15;
    }

    public void setE15(E15 e15) {
        this.e15 = e15;
    }
}
