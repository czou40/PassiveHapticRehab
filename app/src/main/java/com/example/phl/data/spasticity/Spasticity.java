package com.example.phl.data.spasticity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "spasticity")
public class Spasticity implements com.example.phl.data.ProgressData {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "value")
    private Double value;

    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.doubleValue();
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
