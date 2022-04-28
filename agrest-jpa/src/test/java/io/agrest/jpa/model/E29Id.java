package io.agrest.jpa.model;

import java.io.Serializable;

public class E29Id implements Serializable {

    protected Integer id1;
    protected Integer id2;

    public E29Id(Integer id1, Integer id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public E29Id() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        E29Id e29Id = (E29Id) o;

        if (id1 != null ? !id1.equals(e29Id.id1) : e29Id.id1 != null) return false;
        return id2 != null ? id2.equals(e29Id.id2) : e29Id.id2 == null;
    }

    @Override
    public int hashCode() {
        int result = id1 != null ? id1.hashCode() : 0;
        result = 31 * result + (id2 != null ? id2.hashCode() : 0);
        return result;
    }
}