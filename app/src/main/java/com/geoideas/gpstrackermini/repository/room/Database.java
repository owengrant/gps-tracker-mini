package com.geoideas.gpstrackermini.repository.room;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.geoideas.gpstrackermini.repository.room.dao.*;
import com.geoideas.gpstrackermini.repository.room.entity.*;

@androidx.room.Database(
        entities = {
                Point.class, Fence.class, FenceEvent.class,
                User.class, Device.class, Result.class,
                FenceUserAuth.class, QueryCycle.class
        }, version = 7, exportSchema = false
)
public abstract class Database extends RoomDatabase {

    private static Database INSTANCE;

    public abstract PointDao pointDao();
    public abstract FenceDao fenceDao();
    public abstract FenceEventDao fenceEventDao();
    public abstract UserDao userDao();
    public abstract DeviceDao deviceDao();
    public abstract ResultDao resultDao();
    public abstract FenceUserAuthDao fenceUserAuthDao();
    public abstract QueryCycleDao queryCycleDao();

    public static Database getInstance(Context context){
        if(INSTANCE == null){
            synchronized (Database.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context, Database.class, "geoi-sms")
                                   .fallbackToDestructiveMigration()
                                   .build();
                }
            }
        }
        return INSTANCE;
    }

}
