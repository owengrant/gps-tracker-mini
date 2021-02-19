package com.geoideas.gpstrackermini.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.geoideas.gpstrackermini.repository.room.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Query("DELETE FROM user WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM user WHERE code = :code AND name = :username")
    void delete(String code, String username);

    @Query("SELECT * FROM user WHERE code = :code")
    List<User> fetchUser(String code);

    @Query("SELECT * FROM user WHERE id = :id")
    List<User> fetchUser(long id);

    @Query("SELECT * FROM user WHERE phone_number LIKE :phonenumber LIMIT 1")
    List<User> fetchUserByPhonenumber(String phonenumber);

    @Query("SELECT * FROM user WHERE code = :code AND name = :username")
    List<User> fetchUser(String code, String username);

    @Query("SELECT * FROM user ORDER BY name")
    List<User> fetchUsers();

}
