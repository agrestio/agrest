package io.agrest.jpa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "e3")
public class E3 {

    public static final String E2 = "e2";
    public static final String E5 = "e5";
    public static final String NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    protected String name;

    @Column(name = "phone_number")
    protected String phoneNumber;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "e2_id")
    protected E2 e2;

    @ManyToOne (cascade = CascadeType.ALL)
    @JoinColumn(name = "e5_id")
    protected E5 e5;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public E2 getE2() {
        return e2;
    }

    public void setE2(E2 e2) {
        this.e2 = e2;
    }

    public E5 getE5() {
        return e5;
    }

    public void setE5(E5 e5) {
        this.e5 = e5;
    }

    @Override
    public String toString() {
        return "E3{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", e2=" + e2 +
                ", e5=" + e5 +
                '}';
    }
}
