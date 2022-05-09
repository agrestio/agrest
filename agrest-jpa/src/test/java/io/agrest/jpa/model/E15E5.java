package io.agrest.jpa.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// This entity is used only for table e15_e5 cleanup
@Entity
@Table(name = "e15_e5")
public class E15E5 implements Serializable {

    @Id
    private Long e5_id;

    @Id
    private Long e15_id;

}
