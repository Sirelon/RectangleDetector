package com.sirelon.opencv.rectangledetector;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;

/**
 * Created by Sirelon on 04/05/2017.
 */
public interface IView {

    void showSourceImage(Mat src);

    void showResultImage(Mat dst);

    void drawRectangle(Rect rect);

    void drawCircles(Point... points);

    void drawContours(List<MatOfPoint> contours, int maxId);
}
