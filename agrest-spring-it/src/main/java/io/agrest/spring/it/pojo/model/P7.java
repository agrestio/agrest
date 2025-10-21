package io.agrest.spring.it.pojo.model;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

public class P7 {

    int id;
    String string;

    @AgId
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @AgAttribute
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
