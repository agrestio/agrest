package org.example.entity;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

import java.time.LocalDate;

public class P3 {

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

    @AgAttribute
    public LocalDate getD() {
        return null;
    }
}
