package io.agrest.jpa.model;


import jakarta.persistence.*;


@Entity
@Table(name = "e18" )
public  class E18  {

    public static final String E17 = "e17";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String name;

    @ManyToOne
    @JoinColumn (name = "e17_id1")
    @JoinColumn (name = "e17_id2")
    protected E17 e17;

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

    public E17 getE17() {
        return e17;
    }

    public void setE17(E17 e17) {
        this.e17 = e17;
    }
}
