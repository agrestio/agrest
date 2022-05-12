package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "e12_e13")
public class E12E13 implements Serializable {

    @Id
    private Integer e12_id;

    @Id
    private Integer e13_id;


    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "e12_id")
    protected E12 e12;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "e13_id")
    protected E13 e13;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        E12E13 e12E13 = (E12E13) o;

        if (e12_id != null ? !e12_id.equals(e12E13.e12_id) : e12E13.e12_id != null) return false;
        if (e13_id != null ? !e13_id.equals(e12E13.e13_id) : e12E13.e13_id != null) return false;
        if (e12 != null ? !e12.equals(e12E13.e12) : e12E13.e12 != null) return false;
        return e13 != null ? e13.equals(e12E13.e13) : e12E13.e13 == null;
    }

    @Override
    public int hashCode() {
        int result = e12_id != null ? e12_id.hashCode() : 0;
        result = 31 * result + (e13_id != null ? e13_id.hashCode() : 0);
        result = 31 * result + (e12 != null ? e12.hashCode() : 0);
        result = 31 * result + (e13 != null ? e13.hashCode() : 0);
        return result;
    }
}


