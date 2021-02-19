package com.geoideas.gpstrackermini.repository.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "name")
    @NotNull
    private String name;

    @ColumnInfo(name = "code")
    private String code = String.valueOf(new Random().nextInt(10000));

    @ColumnInfo(name = "phone_number")
    @NotNull
    private String phoneNumber;

    @ColumnInfo(name = "request_location")
    private boolean requestLocation = true;

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

    public void setCode(String code) {
        this.code = code;
    }

    @NotNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NotNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(boolean requestLocation) {
        this.requestLocation = requestLocation;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", requestLocation=" + requestLocation +
                '}';
    }
}
