package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "e15_e1")
public class E15E1 implements Serializable {

    @Id
    private Integer e1_id;

    @Id
    private Long e15_id;


    @OneToOne
    @JoinColumn (name = "e1_id")
    protected E1 e1;

    @OneToOne
    @JoinColumn (name = "e15_id")
    protected E15 e15;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        E15E1 e15E1 = (E15E1) o;

        if (e1_id != null ? !e1_id.equals(e15E1.e1_id) : e15E1.e1_id != null) return false;
        if (e15_id != null ? !e15_id.equals(e15E1.e15_id) : e15E1.e15_id != null) return false;
        if (e1 != null ? !e1.equals(e15E1.e1) : e15E1.e1 != null) return false;
        return e15 != null ? e15.equals(e15E1.e15) : e15E1.e15 == null;
    }

    @Override
    public int hashCode() {
        int result = e1_id != null ? e1_id.hashCode() : 0;
        result = 31 * result + (e15_id != null ? e15_id.hashCode() : 0);
        result = 31 * result + (e1 != null ? e1.hashCode() : 0);
        result = 31 * result + (e15 != null ? e15.hashCode() : 0);
        return result;
    }
}
