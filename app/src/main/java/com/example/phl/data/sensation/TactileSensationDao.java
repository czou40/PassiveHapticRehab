package com.example.phl.data.sensation;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface TactileSensationDao {

    @Insert
    long insert(TactileSensation tactileSensation);

    @Query("SELECT * FROM tactile_sensation")
    List<TactileSensation> getAllTactileSensations();

    @Query("SELECT * FROM tactile_sensation WHERE id = :id")
    TactileSensation getTactileSensationById(int id);

    @Query("UPDATE tactile_sensation SET value = :value WHERE id = :id")
    void updateTactileSensation(int id, int value);

    @Query("DELETE FROM tactile_sensation WHERE id = :id")
    void deleteTactileSensation(int id);
}
