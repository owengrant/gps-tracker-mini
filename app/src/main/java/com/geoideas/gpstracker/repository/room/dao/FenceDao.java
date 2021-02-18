package com.geoideas.gpstracker.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.geoideas.gpstracker.repository.room.entity.Fence;

import java.util.List;

@Dao
public interface FenceDao {

    @Insert
    void insert(Fence f);

    @Insert
    void insert(Fence... f);

    @Update
    void update(Fence f);

    @Query("DELETE FROM fence WHERE fence_key = :key")
    void delete(String key);

    @Query("SELECT * FROM fence ORDER BY id DESC")
    List<Fence> fetchAll();

    @Query("SELECT * FROM fence WHERE fence_key = :key ORDER BY id ASC LIMIT 1")
    List<Fence> fetchByKey(String key);

    @Query("SELECT max(id) FROM fence")
    long findLast();

    @Query("SELECT count(id) FROM fence")
    int count();
}
