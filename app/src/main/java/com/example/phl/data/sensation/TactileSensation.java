package com.example.phl.data.sensation;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "tactile_sensation")
public class TactileSensation implements com.example.phl.data.ProgressData {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "value")
    private Integer value;

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

    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Number value) {
        this.value = value.intValue();
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
