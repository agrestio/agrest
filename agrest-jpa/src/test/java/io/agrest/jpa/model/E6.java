package io.agrest.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "e6")
public  class E6  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "char_id")
    private Long char_id;

    @Column(name = "charColumn")
    protected String charColumn;

    public Long getChar_id() {
        return char_id;
    }

    public void setChar_id(Long char_id) {
        this.char_id = char_id;
    }

    public String getCharColumn() {
        return charColumn;
    }

    public void setCharColumn(String charColumn) {
        this.charColumn = charColumn;
    }
}
