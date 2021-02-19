package com.geoideas.gpstrackermini.repository.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.geoideas.gpstrackermini.util.AppConstant;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Random;

@Entity(tableName = "fence")
public class Fence implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name = "id")
    private long id;

    @NotNull
    private String title;

    private String description;

    @ColumnInfo(name = "fence_key")
    @NotNull
    private String key = String.valueOf(new Random().nextInt(100000000));

    @NotNull
    private boolean active = true;

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotNull
    private float radius = 100f;

    @NotNull
    private  boolean enter = true;

    @NotNull
    private  boolean exit = true;

    @NotNull
    private  boolean dwell = true;

    @NotNull
    private String user = AppConstant.LOCAL_USER;

    @NotNull
    private boolean sms = true;

    @NotNull
    private String from;

    @NotNull
    private String to;

    @NotNull
    private String days;

    @NonNull
    private boolean notify = true;

    @NonNull
    private boolean safe = true;

    public Fence(){}

    public Fence(String title, String description, double latitude, double longitude, float radius, boolean enter, boolean exit, boolean sms, boolean active, String days, String from, String to, boolean notify, boolean safe) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.enter = enter;
        this.exit = exit;
        this.active = active;
        this.sms = sms;
        this.days = days;
        this.from = from;
        this.to = to;
        this.notify = notify;
        this.safe = safe;
    }

    public Fence(String title, double latitude, double longitude, float radius, boolean enter, boolean exit, boolean sms, boolean active, String days, String from, String to, boolean notify, boolean safe) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.enter = enter;
        this.exit = exit;
        this.active = active;
        this.sms = sms;
        this.days = days;
        this.from = from;
        this.to = to;
        this.notify = notify;
        this.safe = safe;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(@NotNull String key) {
        this.key = key;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @NotNull
    public String getUser() {
        return user;
    }

    public void setUser(@NotNull String user) {
        this.user = user;
    }

    public boolean isEnter() {
        return enter;
    }

    public void setEnter(boolean enter) {
        this.enter = enter;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isDwell() {
        return dwell;
    }

    public void setDwell(boolean dwell) {
        this.dwell = dwell;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    @NotNull
    public String getFrom() {
        return from;
    }

    public void setFrom(@NotNull String from) {
        this.from = from;
    }

    @NotNull
    public String getTo() {
        return to;
    }

    public void setTo(@NotNull String to) {
        this.to = to;
    }

    @NotNull
    public String getDays() {
        return days;
    }

    public void setDays(@NotNull String days) {
        this.days = days;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    @Override
    public String toString() {
        return "Fence{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", key='" + key + '\'' +
                ", active=" + active +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", enter=" + enter +
                ", exit=" + exit +
                ", dwell=" + dwell +
                ", user='" + user + '\'' +
                ", sms=" + sms +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", days='" + days + '\'' +
                ", notify=" + notify +
                ", safe=" + safe +
                '}';
    }
}

