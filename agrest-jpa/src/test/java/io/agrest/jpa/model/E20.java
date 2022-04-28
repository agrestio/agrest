package io.agrest.jpa.model;

import io.agrest.annotation.AgId;
import jakarta.persistence.*;


@Entity
@Table (name = "e20")
public  class E20   {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected Integer age;

    protected String description;

    @Column(name = "name_col")
    protected String name;

    @OneToOne (cascade = CascadeType.REMOVE)
    protected E21 e21;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }

    @AgId
    public String getName() {
        return name;
    }

    public E21 getE21() {
        return e21;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setE21(E21 e21) {
        this.e21 = e21;
    }
}
