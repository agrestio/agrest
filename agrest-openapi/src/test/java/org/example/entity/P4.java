package org.example.entity;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

public class P4 {

    @AgId
    public int getA() {
        return -1;
    }

    @AgAttribute
    public int getB() {
        return -1;
    }

    @AgAttribute
    public String getC() {
        return "";
    }
}
