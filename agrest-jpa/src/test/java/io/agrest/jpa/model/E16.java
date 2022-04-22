package io.agrest.jpa.model;


import jakarta.persistence.*;


import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table (name = "e16")
public  class E16  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column (name = "c_date")
    protected Date cDate;
    @Column (name = "c_time")
    protected Time cTime;
    @Column (name = "c_timestamp")
    protected Timestamp cTimestamp;

    public Date getcDate() {
        return cDate;
    }

    public void setcDate(Date cDate) {
        this.cDate = cDate;
    }

    public Time getcTime() {
        return cTime;
    }

    public void setcTime(Time cTime) {
        this.cTime = cTime;
    }

    public Timestamp getcTimestamp() {
        return cTimestamp;
    }

    public void setcTimestamp(Timestamp cTimestamp) {
        this.cTimestamp = cTimestamp;
    }
}
