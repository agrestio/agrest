package io.agrest.jpa.model;

import io.agrest.annotation.AgId;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table (name = "e21")
public  class E21  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected Integer age;
    protected String description;
    protected String name;


    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "e21_id")
    protected List<E20> e20s = new java.util.ArrayList<>();

    public void setE20s(List<E20> e20s) {
        this.e20s = e20s;
    }

    public List<E20> getE20s() {
        return e20s;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @AgId
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @AgId
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void setE20s(List<E20> e20s) {
//        this.e20s = e20s;
//    }
//
//    public List<E20> getE20s() {
//        return e20s;
//    }


}
