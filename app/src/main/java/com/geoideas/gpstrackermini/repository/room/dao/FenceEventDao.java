package com.geoideas.gpstrackermini.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.geoideas.gpstrackermini.repository.room.entity.FenceEvent;

import java.util.List;

@Dao
public interface FenceEventDao {

    @Insert
    void insert(FenceEvent fenceEvent);

    @Query("SELECT * FROM fence_event")
    List<FenceEvent> fetchAll();

    @Insert
    void insert(FenceEvent... fenceEvents);

    @Query("SELECT * FROM fence_event WHERE fence_key = :key")
    List<FenceEvent> fetchByKey(String key);

    @Query("SELECT * FROM fence_event WHERE fence_key = :key AND id > :id")
    List<FenceEvent> fetchAfter(String key, long id);


}
