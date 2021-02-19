package com.geoideas.gpstrackermini.repository.room.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "fence_user_auth",
        foreignKeys = {
                @ForeignKey(
                    entity = Fence.class,
                    parentColumns = "id",
                    childColumns = "fid",
                    onDelete = CASCADE
                ),
                @ForeignKey(
                    entity = User.class,
                    parentColumns = "id",
                    childColumns = "uid",
                    onDelete = CASCADE
                )
        }
)
public class FenceUserAuth {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate=true)
    private long id;

    @NotNull
    private long uid;

    @NotNull
    private long fid;

    public FenceUserAuth(long uid, long fid) {
        this.uid = uid;
        this.fid = fid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    @Override
    public String toString() {
        return "FenceUserAuth{" +
                "id=" + id +
                ", uid=" + uid +
                ", fid=" + fid +
                '}';
    }
}
