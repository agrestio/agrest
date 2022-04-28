package io.agrest.jpa.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "e3")
//@TableGenerator(name="tab", initialValue=2000)
//@SequenceGenerator(name="seq", initialValue=2000, allocationSize = 100)
//@SequenceGenerator(name="seq", initialValue=2000)
public class E3 {

    public static final String E2 = "e2";
    public static final String E5 = "e5";
    public static final String NAME = "name";

//
//    @Id
//
//    @GeneratedValue( strategy = GenerationType.SEQUENCE,generator = "mySeqGen")
//
//    @SequenceGenerator(name = "mySeqGen", sequenceName = "MYSEQ", initialValue = 2000, allocationSize = 10000)
    @Id
    @GeneratedValue (generator = "idGenerator")
    @GenericGenerator(name = "idGenerator",strategy = "sequence",
            parameters = {
                    @Parameter(name = "initial_value", value = "2000"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Integer id;

  /*  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;*/

    protected String name;

    @Column(name = "phone_number")
    protected String phoneNumber;

    @ManyToOne (cascade = CascadeType.REMOVE)
    @JoinColumn(name = "e2_id")
    protected E2 e2;

    @ManyToOne (cascade = CascadeType.REMOVE)
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
