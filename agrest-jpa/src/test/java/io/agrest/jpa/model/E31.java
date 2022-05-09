package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "e31")
@IdClass(E31IdClass.class)
public class E31 {

    public static final String ID1 = "id1";
    public static final String ID2 = "id2";

    @Id
    protected Integer id1;

    @Id
    protected Integer id2;

    @Column
    protected String name;

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

    public String getName() {
        return name;
    }

}
