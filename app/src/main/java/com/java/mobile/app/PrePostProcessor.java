// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package com.java.mobile.app;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Math;
import java.util.List;

class Result {
    int classIndex;
    Float score;
    Rect rect;

    public Result(int cls, Float output, Rect rect) {
        this.classIndex = cls;
        this.score = output;
        this.rect = rect;
    }
};

public class PrePostProcessor {
    // for yolov5 model, no need to apply MEAN and STD
    static float[] NO_MEAN_RGB = new float[] {0.0f, 0.0f, 0.0f};
    static float[] NO_STD_RGB = new float[] {1.0f, 1.0f, 1.0f};

    // model input image size
    static int mInputWidth = 640;
    static int mInputHeight = 640;

    // model output is of size 25200*(num_of_class+5)
    private static int mOutputRow = 25200; // as decided by the YOLOv5 model for input image of size 640*640
    private static int mOutputColumn = 9; // left, top, right, bottom, score and 80 class probability
    private static float mThreshold = 0.30f; // score above which a detection is generated
    private static int mNmsLimit = 15;

    static String[] mClasses;

    // The two methods nonMaxSuppression and IOU below are ported from https://github.com/hollance/YOLO-CoreML-MPSNNGraph/blob/master/Common/Helpers.swift
    /**
     Removes bounding boxes that overlap too much with other boxes that have
     a higher score.
     - Parameters:
     - boxes: an array of bounding boxes and their scores
     - limit: the maximum number of boxes that will be selected
     - threshold: used to decide whether boxes overlap too much
     */
    static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {

        // Do an argsort on the confidence scores, from high to low.
        Collections.sort(boxes,
                new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return o1.score.compareTo(o2.score);
                    }
                });

        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        boolean done = false;
        for (int i=0; i<boxes.size() && !done; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit) break;

                for (int j=i+1; j<boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }

    /**
     Computes intersection-over-union overlap between two bounding boxes.
     */
    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }

    static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float ivScaleX, float ivScaleY, float startX, float startY) {
        ArrayList<Result> results = new ArrayList<>();
        for (int i = 0; i< mOutputRow; i++) {
            if (outputs[i* mOutputColumn +4] > mThreshold) {
                float x = outputs[i* mOutputColumn];
                float y = outputs[i* mOutputColumn +1];
                float w = outputs[i* mOutputColumn +2];
                float h = outputs[i* mOutputColumn +3];

                float left = imgScaleX * (x - w/2);
                float top = imgScaleY * (y - h/2);
                float right = imgScaleX * (x + w/2);
                float bottom = imgScaleY * (y + h/2);

                float max = outputs[i* mOutputColumn +5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn -5; j++) {
                    if (outputs[i* mOutputColumn +5+j] > max) {
                        max = outputs[i* mOutputColumn +5+j];
                        cls = j;
                    }
                }

                Rect rect = new Rect((int)(startX+ivScaleX*left), (int)(startY+top*ivScaleY), (int)(startX+ivScaleX*right), (int)(startY+ivScaleY*bottom));
                Result result = new Result(cls, outputs[i*mOutputColumn+4], rect);
                results.add(result);
            }
        }
        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }

    static List<Float> calcDistance(Result result, float x, float y, int x_shape, int y_shape) {
        // parameters = [x_mid_rect, y_mid_rect, start_x, start_y, endpoint_x, endpoint_y, distance, collisionAlert]
        List<Float> parameters = new ArrayList<Float>();
        float collisionAlert = 0;
        System.out.println("Class Index: " + result.classIndex);
        System.out.println("Score: " + result.score);
        System.out.println("Rect: " + result.rect);
        System.out.println("--------------");

        float x1 = result.rect.left, y1 = result.rect.top, x2 = result.rect.right, y2 = result.rect.bottom;
        float x_mid_rect = (x1 + x2) / 2, y_mid_rect = (y1 + y2) / 2;
        float y_line_length = Math.abs(y1 - y2), x_line_length = Math.abs(x1 - x2);

        // draw a circle with center as x_mid_rect and y_mid_rect points
        parameters.addAll(Arrays.asList(x_mid_rect, y_mid_rect));
        List<Float> points = new ArrayList<Float>();
        points.addAll(Arrays.asList(x1, y1, x2, y2, x_mid_rect, y_mid_rect, x_line_length, y_line_length));

        // x_shape, y_shape are dimensions of camera area
        int x_shape_mid = Math.round(x_shape / 2);
        int start_x = x_shape_mid, start_y = y_shape;
        int[] start_point = new int[]{start_x, start_y};

        // draw a circle with start_x and start_y as midpoints
//        parameters.addAll(Arrays.asList((float) start_x, (float) start_y));
        parameters.addAll(Arrays.asList((float) x, (float) y));

        int height_in_rf = 121;
        int measured_distance = 275; // inch = 700 cm
        int real_height = 60; // inch = 150 cm
        float focal_length = (height_in_rf * measured_distance) / real_height;
        float pixel_per_cm = (float) ((2200 / x_shape) * 2.54); // 1 pixel = 0.0264583333


        Float end_x1 = points.get(0), end_y1 = points.get(1);
        Float end_x2 = points.get(2), end_y2 = points.get(3);
        Float end_x_mid_rect = points.get(4), end_y_mid_rect = points.get(5);
        Float end_x_line_length = points.get(6), end_y_line_length = points.get(7);
        Float[] end_point;
        if (end_x2 < x_shape_mid) {
            // For left point
            end_point = new Float[]{end_x2, end_y2}; // Select lower right corner
        } else if (end_x1 > x_shape_mid) {
            // For right point
            end_point = new Float[]{end_x1, end_y2}; // Select bottom left corner
        } else {
            // Vehicle in the middle
            end_point = new Float[]{end_x_mid_rect, end_y2}; // Select the middle of the underline
        }

        // adding endpoints to parameters
        parameters.addAll(Arrays.asList(end_point[0], end_point[1]));

        float dif_x = Math.abs(x - end_point[0]);
        float dif_y = Math.abs(y - end_point[1]);
        double pixel_count = Math.sqrt(Math.pow(dif_x, 2) + Math.pow(dif_y, 2));
        double distance = pixel_count * pixel_per_cm / end_y_line_length;

        // distance = real_height * focal_length / Math.abs(end_y1 - end_y2)
        // distance = distance * 2.54 / 100
        System.out.println(distance + " this is the object distance");
        parameters.add((float) distance);
        if(distance<=2) collisionAlert = 1;
        parameters.add((float) collisionAlert);

        return parameters;
    }
}
