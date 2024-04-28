package com.java.mobile.app;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface dao {

    @Query("SELECT * FROM prediction_logs ORDER BY frameId DESC")
    static List<PredictionLogEntity> getLogsSortedByFrameId() {
        return null;
    }

    @Query("SELECT * FROM prediction_logs WHERE collisionAlert = 1")
    static List<PredictionLogEntity> getLogsForCollisionAlert() {
        return null;
    }

    @Upsert
    static void upsert(PredictionLogEntity prediction) {

    }

    @Delete
    void delete(PredictionLogEntity prediction);

}
