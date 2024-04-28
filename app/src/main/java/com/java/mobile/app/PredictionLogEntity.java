package com.java.mobile.app;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "prediction_logs")
public class PredictionLogEntity {
    // parameters = [x_mid_rect, y_mid_rect, start_x, start_y, endpoint_x, endpoint_y, distance, collisionAlert]
    @PrimaryKey(autoGenerate = true)
    private long frameId = 0;
    private float xMidRect;
    private float yMidRect;
    private float startX;
    private float startY;
    private float endpointX;
    private float endpointY;
    private float distance;
    private float collisionAlert;
    private Date timestamp;

    // Constructors, getters, and setters
    public void setFrameId(long frameId) {
        this.frameId = frameId;
    }
    public void setXMidRect(float xMidRect) {
        this.xMidRect = xMidRect;
    }

    public void setYMidRect(float yMidRect) {
        this.yMidRect = yMidRect;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public void setEndpointX(float endpointX) {
        this.endpointX = endpointX;
    }

    public void setEndpointY(float endpointY) {
        this.endpointY = endpointY;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setCollisionAlert(float collisionAlert) {
        this.collisionAlert = collisionAlert;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Getter methods

    public long getFrameId() {
        return frameId;
    }

    public float getXMidRect() {
        return xMidRect;
    }

    public float getYMidRect() {
        return yMidRect;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getEndpointX() {
        return endpointX;
    }

    public float getEndpointY() {
        return endpointY;
    }

    public float getDistance() {
        return distance;
    }

    public float getCollisionAlert() {
        return collisionAlert;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
