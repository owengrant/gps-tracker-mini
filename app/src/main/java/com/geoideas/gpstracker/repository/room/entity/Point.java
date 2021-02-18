package com.geoideas.gpstracker.repository.room.entity;

import android.location.Location;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "point")
public class Point implements Serializable {
    private static final long serialVersionUID = 1l;
    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name = "id")
    private long id;
    @NotNull
    @ColumnInfo(name = "latitude")
    private double latitude;
    @NotNull
    @ColumnInfo(name = "longitude")
    private double longitude;
    @NotNull
    @ColumnInfo(name = "accuracy")
    private double accuracy;
    @NotNull
    @ColumnInfo(name = "moment")
    private String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    public Point fromLocation(Location loc){
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        accuracy = loc.hasAccuracy() ? loc.getAccuracy() : -1;
        moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getMoment() {
        return moment;
    }

    public void setMoment(String moment) {
        this.moment = moment;
    }

    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", moment='" + moment + '\'' +
                '}';
    }
}
