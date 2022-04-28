package io.agrest.jpa.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "e4")
public class E4 {

    public static final String C_TIMESTAMP = "cTimestamp";
    public static final String C_DATE = "cDate";
    public static final String C_TIME = "cTime";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "C_BOOLEAN")
    private Boolean cBoolean;

    @Column(name = "C_DATE")
    private Date cDate;

    @Column(name = "C_DECIMAL")
    private BigDecimal cDecimal;

    @Column(name = "C_INT")
    private Integer cInt;

    @Column(name = "C_TIME")
    private Date cTime;

    @Column(name = "C_TIMESTAMP")
    private Date cTimestamp;

    @Column(name = "C_VARCHAR")
    private String cVarchar;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getCBoolean() {
        return cBoolean;
    }

    public void setCBoolean(Boolean cBoolean) {
        this.cBoolean = cBoolean;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }

    public BigDecimal getCDecimal() {
        return cDecimal;
    }

    public void setCDecimal(BigDecimal cDecimal) {
        this.cDecimal = cDecimal;
    }

    public Integer getCInt() {
        return cInt;
    }

    public void setCInt(Integer cInt) {
        this.cInt = cInt;
    }

    public Date getCTime() {
        return cTime;
    }

    public void setCTime(Date cTime) {
        this.cTime = cTime;
    }

    public Date getCTimestamp() {
        return cTimestamp;
    }

    public void setCTimestamp(Date cTimestamp) {
        this.cTimestamp = cTimestamp;
    }

    public String getCVarchar() {
        return cVarchar;
    }

    public void setCVarchar(String cVarchar) {
        this.cVarchar = cVarchar;
    }

}
