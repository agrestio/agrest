package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "e9")
public class E9 {

    @Id
    protected Integer e8_id;

    @OneToOne
    @JoinColumn(name = "e8_id")
    protected E8 e8;

    public void setE8_id(Integer e8_id) {
        this.e8_id = e8_id;
    }

    public Integer getE8_id() {
        return e8_id;
    }

    public E8 getE8() {
        return e8;
    }

    public void setE8(E8 e8) {
        this.e8 = e8;
    }


}
