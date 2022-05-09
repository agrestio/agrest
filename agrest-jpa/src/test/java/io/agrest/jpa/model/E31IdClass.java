package io.agrest.jpa.model;

import java.io.Serializable;

public class E31IdClass implements Serializable {

    protected Integer id1;
    protected Integer id2;

    public E31IdClass(Integer id1, Integer id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public E31IdClass() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        E31IdClass that = (E31IdClass) o;

        if (id1 != null ? !id1.equals(that.id1) : that.id1 != null) return false;
        return id2 != null ? id2.equals(that.id2) : that.id2 == null;
    }

    @Override
    public int hashCode() {
        int result = id1 != null ? id1.hashCode() : 0;
        result = 31 * result + (id2 != null ? id2.hashCode() : 0);
        return result;
    }
}