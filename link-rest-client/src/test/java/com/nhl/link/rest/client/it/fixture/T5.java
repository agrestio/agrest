package com.nhl.link.rest.client.it.fixture;

import java.util.Collection;
import java.util.Objects;

public class T5 {
    public static final String P_ID = "id";
    public static final String P_T3S = "t3s";

    private Integer id;
    private Collection<T3> t3s;

    public T5(Integer id) {
        this.id = Objects.requireNonNull(id);
    }

    public T5() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Collection<T3> getT3s() {
        return t3s;
    }

    public void setT3s(Collection<T3> t3s) {
        this.t3s = t3s;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        T5 t5 = (T5) object;

        if (!id.equals(t5.id)) return false;
        return t3s != null ? t3s.containsAll(t5.t3s) : t5.t3s == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (t3s != null ? t3s.hashCode() : 0);
        return result;
    }
}
