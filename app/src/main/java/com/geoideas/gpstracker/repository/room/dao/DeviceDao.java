package com.geoideas.gpstracker.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.geoideas.gpstracker.repository.room.entity.Device;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert
    void insert(Device d);

    @Update
    void update(Device d);

    @Query("DELETE FROM device WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM device ORDER BY ID ASC")
    List<Device> findAll();

    @Query("SELECT * FROM device WHERE id = :id")
    List<Device> findById(Long id);

    @Query("SELECT * FROM device WHERE email = :email LIMIT 1")
    List<Device> findByIdEmail(String email);

    @Query("SELECT * FROM device WHERE email LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    List<Device> findByIdEmailOrName(String query);

    @Query("SELECT * FROM device WHERE email = :email AND live_location = :live")
    List<Device> findByEmailAndLive(String email, Boolean live);

    @Query("SELECT * FROM device WHERE live_location = :live AND show_location = :show")
    List<Device> findByLiveAndShow(boolean live, boolean show);
}
