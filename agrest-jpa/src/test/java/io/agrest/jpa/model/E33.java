package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Inheritance (strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class E33 {

    @Id
    @Column(name = "id", nullable = false)
    protected Long id;

    protected String firstName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
