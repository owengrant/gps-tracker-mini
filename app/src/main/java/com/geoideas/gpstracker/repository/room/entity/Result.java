package com.geoideas.gpstracker.repository.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "result")
public class Result {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "result")
    @NotNull
    private String result;

    @ColumnInfo(name = "seen")
    @NotNull
    private boolean seen = false;

    @ColumnInfo(name = "moment")
    @NotNull
    private String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    public Result(){}

    public Result(String result) {
        this.result = result;
    }

    public Result(String result, boolean seen) {
        this.seen = seen;
        this.result = result;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getResult() {
        return result;
    }

    public void setResult(@NotNull String result) {
        this.result = result;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @NotNull
    public String getMoment() {
        return moment;
    }

    public void setMoment(@NotNull String moment) {
        this.moment = moment;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", result='" + result + '\'' +
                ", seen=" + seen +
                ", moment='" + moment + '\'' +
                '}';
    }
}
