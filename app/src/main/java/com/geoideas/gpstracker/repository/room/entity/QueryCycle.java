package com.geoideas.gpstracker.repository.room.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "query-cycle",
        foreignKeys = {
            @ForeignKey(
                    entity = Device.class,
                    parentColumns = "id",
                    childColumns = "did",
                    onDelete = CASCADE
            )
        }
)
public class QueryCycle implements Serializable {
    private static final long serialVersionUID = 1l;

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NotNull
    private long did;
    @NotNull
    private int rcode;
    @NotNull
    private String status;
    @NotNull
    private String moment = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    @NotNull
    private boolean seen = false;
    @NotNull
    private String request;
    private String response;

    public QueryCycle() {}

    public QueryCycle(long did, @NotNull String status, @NotNull int rCode, @NotNull String request) {
        this.did = did;
        this.status = status;
        this.rcode = rCode;
        this.request = request;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    @NotNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NotNull String status) {
        this.status = status;
    }

    @NotNull
    public String getMoment() {
        return moment;
    }

    public void setMoment(@NotNull String moment) {
        this.moment = moment;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @NotNull
    public String getRequest() {
        return request;
    }

    public void setRequest(@NotNull String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getRcode() {
        return rcode;
    }

    public void setRcode(int rcode) {
        this.rcode = rcode;
    }
}
