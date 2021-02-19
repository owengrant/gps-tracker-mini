package com.geoideas.gpstrackermini.repository.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.geoideas.gpstrackermini.repository.room.entity.FenceUserAuth;
import com.geoideas.gpstrackermini.repository.room.entity.User;

import java.util.List;

@Dao
public interface FenceUserAuthDao {

    @Insert
    void insert(FenceUserAuth fenceUserAuth);

    @Query("DELETE FROM fence_user_auth WHERE fid = :fid")
    void delete(long fid);

    @Query("DELETE FROM fence_user_auth WHERE fid = :fid AND uid = :uid")
    void removeUser(long fid, long uid);

    @Query("SELECT count(id) FROM fence_user_auth WHERE fid = :fid AND uid = :uid")
    int hasAccess(long fid,  long uid);

    @Query(
            "SELECT user.id, user.name, user.request_location, user.phone_number " +
            "FROM user INNER JOIN fence_user_auth " +
            "ON user.id = fence_user_auth.uid " +
            "WHERE fid = :fid"
    )
    List<User> users(long fid);

}

