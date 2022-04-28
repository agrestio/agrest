package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Table (name = "e7")
public  class E7  {

    public static final String NAME = "name";
    public static final String E8 = "e8";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String name;

    @ManyToOne
    @JoinColumn(name = "e8_id")
    protected E8 e8;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public E8 getE8() {
        return e8;
    }

    public void setE8(E8 e8) {
        this.e8 = e8;
    }
}
