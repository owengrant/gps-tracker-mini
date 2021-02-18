package com.geoideas.gpstracker.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.geoideas.gpstracker.repository.room.entity.Result;

import java.util.List;

@Dao
 public interface ResultDao {

    @Insert
    void insert(Result r);

    @Update
    void markSeen(Result r);

    @Query("DELETE FROM result WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM result WHERE seen = 'false' ORDER BY id ASC")
    List<Result> fetchUnSeen();
}
