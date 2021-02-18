package com.geoideas.gpstracker.repository.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "device")
public class Device implements Serializable {
    private static final long serialVersionUID = 1l;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "name")
    @NotNull
    private String name;

    @ColumnInfo(name = "code")
    @NotNull
    private String code;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "phone_number")
    @NotNull
    private String phoneNumber;

    @ColumnInfo(name = "decsription")
    @NotNull
    private String description;

    @ColumnInfo(name = "latitude")
    @NotNull
    private double latitude = 0.0;

    @ColumnInfo(name = "longitude")
    @NotNull
    private double longitude = 0.0;

    @ColumnInfo(name = "moment")
    @NotNull
    private String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    @ColumnInfo(name = "live_location")
    @NotNull
    private boolean liveLocation = false;

    @ColumnInfo(name = "show_location")
    @NotNull
    private boolean showLocation = false;

    @NotNull
    @ColumnInfo(name = "accuracy")
    private double accuracy;

    public Device(){}

    public Device(
            @NotNull String name,
            @NotNull String code,
            @NotNull String email,
            @NotNull String phoneNumber,
            @NotNull String description
    ) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getCode() {
        return code;
    }

    public void setCode(@NotNull String code) {
        this.code = code;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    @NotNull
    public String getMoment() {
        return moment;
    }

    public void setMoment(@NotNull String moment) {
        this.moment = moment;
    }

    public boolean isLiveLocation() {
        return liveLocation;
    }

    public void setLiveLocation(boolean liveLocation) {
        this.liveLocation = liveLocation;
        this.showLocation = liveLocation;
    }

    public boolean isShowLocation() {
        return showLocation;
    }

    public void setShowLocation(boolean showLocation) {
        this.showLocation = showLocation;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @NotNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", moment='" + moment + '\'' +
                ", liveLocation=" + liveLocation +
                ", showLocation=" + showLocation +
                ", accuracy=" + accuracy +
                '}';
    }
}
