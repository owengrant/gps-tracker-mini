package com.geoideas.gpstracker.repository.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "fence_event")
public class FenceEvent {

    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "fence_key")
    @NotNull
    private String key;

    @ColumnInfo(name = "transition")
    @NotNull
    private int transition;

    @ColumnInfo(name = "latitude")
    @NotNull
    private double latitude;

    @ColumnInfo(name = "longitude")
    @NotNull
    private double longitude;

    @NotNull
    @ColumnInfo(name = "accuracy")
    private double accuracy;

    @ColumnInfo(name = "moment")
    @NotNull
    private String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(@NotNull String key) {
        this.key = key;
    }

    public int getTransition() {
        return transition;
    }

    public void setTransition(int transition) {
        this.transition = transition;
    }

    @NotNull
    public String getMoment() {
        return moment;
    }

    public void setMoment(@NotNull String moment) {
        this.moment = moment;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "FenceEvent{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", transition=" + transition +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", moment='" + moment + '\'' +
                '}';
    }
}
