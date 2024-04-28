package com.java.mobile.app;

import java.util.Date;
import java.util.List;

public class PredictionLogRepository {
    private static dao predictionLogDao;

    static {
        predictionLogDao = null;
    }

    public PredictionLogRepository(dao predictionLogDao) {
        this.predictionLogDao = predictionLogDao;
    }

    public static void insertPredictionLog(float xMidRect, float yMidRect, float startX, float startY,
                                           float endpointX, float endpointY, float distance, float collisionAlert, Date timestamp) {
        PredictionLogEntity prediction = new PredictionLogEntity();
        prediction.setXMidRect(xMidRect);
        prediction.setYMidRect(yMidRect);
        prediction.setStartX(startX);
        prediction.setStartY(startY);
        prediction.setEndpointX(endpointX);
        prediction.setEndpointY(endpointY);
        prediction.setDistance(distance);
        prediction.setCollisionAlert(collisionAlert);
        prediction.setTimestamp(timestamp);

        dao.upsert(prediction);
    }

    public List<PredictionLogEntity> getPredictionLogsForCollisionAlert() {
        return dao.getLogsForCollisionAlert();
    }

    public List<PredictionLogEntity> getAllPredictionLogsSortedByFrameId() {
        return dao.getLogsSortedByFrameId();
    }

    public void insertPredictionLog(PredictionLogEntity prediction) {
        dao.upsert(prediction);
    }

    // Add other methods as needed
}
