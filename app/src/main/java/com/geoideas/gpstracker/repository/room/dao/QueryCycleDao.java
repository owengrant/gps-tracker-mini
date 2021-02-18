package com.geoideas.gpstracker.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.geoideas.gpstracker.repository.room.entity.QueryCycle;

import java.util.List;

@Dao
public interface QueryCycleDao {

    @Insert
    void insert(QueryCycle qs);

    @Update
    void update(QueryCycle qs);

    @Query("DELETE FROM `query-cycle` WHERE did = :did")
    void delete(long did);

    @Query("DELETE FROM `query-cycle` WHERE id = :id AND did = :did")
    void delete(long id, long did);

    @Query("DELETE FROM `query-cycle` WHERE did = :did")
    void deleteByDeviceId(long did);

    @Query("SELECT * FROM `query-cycle` WHERE did = :did")
    List<QueryCycle> findByDeviceId(long did);

    @Query("SELECT * FROM `query-cycle` WHERE did = :did ORDER BY id ASC")
    List<QueryCycle> findByDeviceIdOrderById(long did);

    @Query("SELECT * FROM `query-cycle` WHERE did = :did ORDER BY datetime(moment) ASC")
    List<QueryCycle> findByDeviceIdOrderByMoment(long did);

    @Query("SELECT * FROM `query-cycle` WHERE rcode = :rCode LIMIT 1")
    QueryCycle findByRCode(int rCode);

}
