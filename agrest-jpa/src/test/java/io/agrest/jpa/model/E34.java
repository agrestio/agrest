package io.agrest.jpa.model;

import jakarta.persistence.Entity;

@Entity
public class E34 extends E33 {

    private String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
