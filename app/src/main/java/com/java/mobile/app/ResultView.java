// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package com.java.mobile.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ResultView extends View {

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint roiRectangle;
    private Paint mPaintText;
    private ArrayList<Result> mResults;
    private List<Float> mParameters;
    private Paint centerROI;
    private Paint centerBoundingBox; // x_mid_rect, y_mid_rect
    private Paint centerBottomLine; // start_x and start_y
    private Paint linearDistance;


    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();

        roiRectangle = new Paint();
        roiRectangle.setColor(Color.BLUE);

        centerROI = new Paint();
        centerROI.setColor(Color.RED);

        centerBoundingBox = new Paint();
        centerBoundingBox.setColor(Color.RED);

        centerBottomLine = new Paint();
        centerBottomLine.setColor(Color.RED);

        linearDistance = new Paint();
        linearDistance.setColor(Color.RED);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mResults == null) return;
        for (Result result : mResults) {
            mPaintRectangle.setStrokeWidth(5);
            mPaintRectangle.setStyle(Paint.Style.STROKE);
            canvas.drawRect(result.rect, mPaintRectangle);

            Path mPath = new Path();
            RectF mRectF = new RectF(result.rect.left, result.rect.top, result.rect.left + TEXT_WIDTH,  result.rect.top + TEXT_HEIGHT);
            mPath.addRect(mRectF, Path.Direction.CW);
            mPaintText.setColor(Color.MAGENTA);
            canvas.drawPath(mPath, mPaintText);

            mPaintText.setColor(Color.WHITE);
            mPaintText.setStrokeWidth(0);
            mPaintText.setStyle(Paint.Style.FILL);
            mPaintText.setTextSize(32);
            canvas.drawText(String.format("%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);

            roiRectangle.setStrokeWidth(10);
            roiRectangle.setStyle(Paint.Style.STROKE);

            // System.out.println("frame width: " + canvas.getWidth() + " frame height: " + canvas.getHeight());
            // frame width = 1080
            // frame height = 1934
            float left = 40 + 200, top = 17 + 500;
            float width = canvas.getWidth() - left, height = canvas.getHeight() - 17 - 100;
            RectF myRectum = new RectF(left, top, width, height);
            canvas.drawRect(myRectum, roiRectangle);

            float x = (width + left) / 2;
            float y = height;
            // System.out.println(x+" "+y);

            centerROI.setStrokeWidth(10);
            centerROI.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(x,y,7, centerROI);

            centerBoundingBox.setStrokeWidth(10);
            centerBoundingBox.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mParameters.get(0), mParameters.get(1), 7, centerBoundingBox );

            linearDistance.setStrokeWidth(10);
            linearDistance.setStyle(Paint.Style.STROKE);
            canvas.drawLine(mParameters.get(2), mParameters.get(3), mParameters.get(4), mParameters.get(5), linearDistance);

        }
    }

    public void setResults(ArrayList<Result> results) {
        mResults = results;
    }
    public void getDetectionParameters(List<Float> parameters){ mParameters = parameters; }
}
