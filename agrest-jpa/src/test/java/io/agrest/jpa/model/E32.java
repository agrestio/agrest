package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "e32")
public class E32 {

    @EmbeddedId
    protected E32EmbeddedIdClass id;

    @Column
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
