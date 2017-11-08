package com.nhl.link.rest.it.fixture;

import java.util.Collection;
import java.util.Objects;

public class T3 {
    public static final String P_ID = "id";
    public static final String P_T4S = "t4s";
    public static final String P_T5 = "t5";

    private Integer id;
    private Collection<T4> t4s;
    private T5 t5;

    public T3(Integer id) {
        this.id = Objects.requireNonNull(id);
    }

    public T3() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Collection<T4> getT4s() {
        return t4s;
    }

    public void setT4s(Collection<T4> t4s) {
        this.t4s = t4s;
    }

    public T5 getT5() {
        return t5;
    }

    public void setT5(T5 t5) {
        this.t5 = t5;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        T3 t3 = (T3) object;

        if (!id.equals(t3.id)) return false;
        if (t4s != null ? !t4s.containsAll(t3.t4s) : t3.t4s != null) return false;
        return t5 != null ? t5.equals(t3.t5) : t3.t5 == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (t4s != null ? t4s.hashCode() : 0);
        result = 31 * result + (t5 != null ? t5.hashCode() : 0);
        return result;
    }
}
