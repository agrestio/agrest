package io.agrest.jpa.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table (name = "e30")
public  class E30  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    protected E29 e29;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public E29 getE29() {
        return e29;
    }

    public void setE29(E29 e29) {
        this.e29 = e29;
    }
}
