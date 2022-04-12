package io.agrest.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table (name = "e28")
public  class E28 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //FIXME type of Json?
//    @Column (name = "json")
//    protected Json json;

    public Long getId() {
        return id;
    }



}
