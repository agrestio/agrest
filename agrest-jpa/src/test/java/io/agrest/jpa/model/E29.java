package io.agrest.jpa.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table (name = "e29")
@IdClass(E29Id.class)
public  class E29  {

    public static final String ID1 = "id1";
    public static final String ID2 = "id2";
    public static final String ID2PROP = "id2Prop";
    public static final String E30S = "e30s";

    @Id
    private Integer id1;

    @Id
    private Integer id2;

//    @Column (name = "ID2PROP")
//    protected Integer id2Prop;

    @OneToMany(cascade = {CascadeType.REMOVE})
    protected List<E30> e30s = new java.util.ArrayList<>();

//    public Integer getId2Prop() {
//        return id2Prop;
//    }
//
//    public void setId2Prop(Integer id2Prop) {
//        this.id2Prop = id2Prop;
//    }

    public List<E30> getE30s() {
        return e30s;
    }

    public void setE30s(List<E30> e30s) {
        this.e30s = e30s;
    }


}
