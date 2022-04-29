package io.agrest.jpa.model;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;


@Entity
@Table (name = "e17")
public  class E17 implements Serializable {

    public static final String ID1 = "id1";
    public static final String ID2 = "id2";
    public static final String E18S = "e18s";

    @Id
    protected Integer id1;

    @Id
    protected Integer id2;

    @Column
    protected String name;

    @OneToMany (mappedBy = "e17")
    protected List<E18> e18s = new java.util.ArrayList<>();

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setE18s(List<E18> e18s) {
        this.e18s = e18s;
    }

    public List<E18> getE18s() {
        return e18s;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        E17 e17 = (E17) o;

        if (id1 != null ? !id1.equals(e17.id1) : e17.id1 != null) return false;
        if (id2 != null ? !id2.equals(e17.id2) : e17.id2 != null) return false;
        return name != null ? name.equals(e17.name) : e17.name == null;
    }

    @Override
    public int hashCode() {
        int result = id1 != null ? id1.hashCode() : 0;
        result = 31 * result + (id2 != null ? id2.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
