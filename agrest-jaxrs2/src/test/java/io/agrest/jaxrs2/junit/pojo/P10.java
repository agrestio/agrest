package io.agrest.jaxrs2.junit.pojo;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

import java.util.HashMap;
import java.util.Map;

public class P10 {

    private int id1;
    private String id2;
    private String a1;

    public static Map<String, Object> id(int id1, String id2) {
        Map<String, Object> id = new HashMap<>();
        id.put("id1", id1);
        id.put("id2", id2);
        return id;
    }

    public Map<String, Object> id() {
        return P10.id(id1, id2);
    }

    @AgId
    public int getId1() {
        return id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    @AgId
    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    @AgAttribute
    public String getA1() {
        return a1;
    }

    public void setA1(String a1) {
        this.a1 = a1;
    }
}
