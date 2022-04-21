package io.agrest.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "e6")
public class E6  {

    @Id
    @Column(name = "CHAR_ID")
    private String id;

    @Column(name = "CHAR_COLUMN")
    protected String charColumn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCharColumn() {
        return charColumn;
    }

    public void setCharColumn(String charColumn) {
        this.charColumn = charColumn;
    }
}
