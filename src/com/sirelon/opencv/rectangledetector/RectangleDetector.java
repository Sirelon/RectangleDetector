package com.sirelon.opencv.rectangledetector;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Sirelon on 04/05/2017.
 */
public class RectangleDetector {



    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private static Mat rotateMat(Mat src, double angle, double scale) {
        Point center = new Point(src.cols() / 2, src.rows() / 2);
        Mat rotated = Imgproc.getRotationMatrix2D(center, angle, scale);
        Mat dst = new Mat();

        Imgproc.warpAffine(src, dst, rotated, dst.size());
        return dst;
    }
}
