package com.example.phl.data.spasticity;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.phl.data.spasticity.Spasticity;

import java.util.List;

@Dao
public interface SpasticityDao {

    @Insert
    long insert(Spasticity spasticity);

    @Query("SELECT * FROM spasticity")
    List<Spasticity> getAllSpasticities();

    @Query("SELECT * FROM spasticity WHERE id = :id")
    Spasticity getSpasticityById(int id);

    @Query("UPDATE spasticity SET value = :value WHERE id = :id")
    void updateSpasticity(int id, Double value);

    @Query("DELETE FROM spasticity WHERE id = :id")
    void deleteSpasticity(int id);
}
