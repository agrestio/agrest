package com.nhl.link.rest.client.it.fixture;

import java.util.Objects;

public class T4 {
    public static final String P_ID = "id";
    public static final String P_T3 = "t3";

    private Integer id;
    private T3 t3;

    public T4(Integer id) {
        this.id = Objects.requireNonNull(id);
    }

    public T4() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public T3 getT3() {
        return t3;
    }

    public void setT3(T3 t3) {
        this.t3 = t3;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        T4 t4 = (T4) object;

        if (!id.equals(t4.id)) return false;
        return t3 != null ? t3.equals(t4.t3) : t4.t3 == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (t3 != null ? t3.hashCode() : 0);
        return result;
    }
}
