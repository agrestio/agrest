package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.AgAttribute;

public class P0 {

    private String name;
    private int age;
    private String description;

    @AgAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @AgAttribute
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @AgAttribute
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
