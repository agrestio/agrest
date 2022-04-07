package io.agrest.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "e9")
public class E9 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "e8_id")
    protected E8 e8;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public E8 getE8() {
        return e8;
    }

    public void setE8(E8 e8) {
        this.e8 = e8;
    }
}
