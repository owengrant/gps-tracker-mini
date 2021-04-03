package com.geoideas.gpstrackermini.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.geoideas.gpstrackermini.repository.room.entity.Point;

import java.util.List;

@Dao
public interface PointDao {

    @Insert
    void insert(Point p);

    @Insert
    void insert(Point... p);

    @Query("SELECT * FROM point ORDER BY id ASC")
    List<Point> fetchAll();

    @Query("SELECT * FROM point WHERE moment BETWEEN datetime(:from) AND datetime(:to) ORDER BY id ASC")
    List<Point> fetchBetween(String from, String to);

    @Query("SELECT * FROM point ORDER BY id ASC LIMIT :amt")
    List<Point> fetchLastN(int amt);

    @Query("SELECT * FROM point ORDER BY id ASC LIMIT 1")
    List<Point> fetchLast();

    @Query("SELECT * FROM point WHERE accuracy <= :acc ORDER BY id ASC")
    List<Point> fetchAllAccuracy(double acc);

    @Query("SELECT * FROM point WHERE moment BETWEEN datetime(:from) AND datetime(:to) AND accuracy <= :acc  ORDER BY id ASC")
    List<Point> fetchBetweenAccuracy(String from, String to, double acc);

    @Query("SELECT * FROM point WHERE accuracy <= :acc ORDER BY id DESC LIMIT :amt")
    List<Point> fetchLastNAccuracy(int amt, double acc);

    @Query("SELECT * FROM point WHERe accuracy <= :acc ORDER BY id DESC LIMIT 1")
    List<Point> fetchLastAccuracy(double acc);

    @Query("DELETE FROM point")
    void deleteAll();
}
