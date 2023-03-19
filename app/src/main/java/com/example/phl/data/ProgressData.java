package com.example.phl.data;

import androidx.room.ColumnInfo;

import java.util.Date;

public interface ProgressData {
    public int getId();

    public void setId(int id);

    public Date getDate();

    public void setDate(Date date);

    public Number getValue();

    public void setValue(Number value);
}
