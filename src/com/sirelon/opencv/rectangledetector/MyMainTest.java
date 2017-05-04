package com.sirelon.opencv.rectangledetector;

import com.sirelon.opencv.rectangledetector.util.CVLoader;
import com.sirelon.opencv.rectangledetector.util.ImgWindow;
import com.sun.istack.internal.Nullable;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sirelon on 28/04/2017.
 */
public class MyMainTest {

    private static ImgWindow origWnd;
    private static ImgWindow projWnd;
    private static Mat img;

    private static ILogger logger = new ConsoleLogger();

    public static void main(String[] args) {
        CVLoader.load();
//        img = Highgui.imread("data/imageRot.jpeg");
//        img = Highgui.imread("data/image.jpeg");
        img = Highgui.imread("data/rectPerspHARD.jpg");
//        img = Highgui.imread("data/rectPersp.jpg");
//        img = Highgui.imread("data/test1.png");

        origWnd = ImgWindow.newWindow(img);
        projWnd = ImgWindow.newWindow();

        origWnd.setImage(img);

        while (!projWnd.closed) {
            Mat proj = detectRect(img);
//            Mat proj = perpsect(img);
            if (proj != null) {
                projWnd.setImage(proj);
                break;
            }
        }


    }

    private static Mat perpsect(Mat img) {
        int resW = (int) img.size().width;
        int resH = (int) img.size().height;

        Mat outPutMat = new Mat(resW, resH, CvType.CV_8UC4);

        return null;
    }

    public static Mat detectRect(Mat src) {
        MatOfPoint maxMatOfPoint = findCountours(src);

        if (maxMatOfPoint == null) {
            return src;
        }

        MatOfPoint2f maxMatOfPoint2f = new MatOfPoint2f(maxMatOfPoint.toArray());

        Mat dst = src;

        Rect rect = Imgproc.boundingRect(maxMatOfPoint);

        Core.rectangle(dst, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255), 2);
//        dst = dst.submat(rect);

        List<Point> corners = foundCorners(maxMatOfPoint2f);

        boolean shouldChangePerspective = false;
        // Check if we should correct the perspective
        if (corners.size() == 4) {
            // There is rectangle
            Point p1, p2, p3, p4;

            p1 = corners.get(0);
            p2 = corners.get(1);
            p3 = corners.get(2);
            p4 = corners.get(3);

            int offset = 100;
            double widthTop = p2.x - p1.x;
            double widthBottom = p3.x - p4.x;

            double absWidth = Math.abs(widthTop - widthBottom);
            if (absWidth > offset) {
                shouldChangePerspective = true;
            } else {
                double heightTop = p4.y - p1.y;
                double heightBottom = p3.y - p2.y;

                double absHeight = Math.abs(heightTop - heightBottom);
                if (absHeight > offset) {
                    shouldChangePerspective = true;
                }
            }
        }

        if (shouldChangePerspective) {
            dst = changePerspective(corners, dst);
        } else {
            RotatedRect rotatedRect = Imgproc.minAreaRect(maxMatOfPoint2f);

            double scale = 1.;

            double angle = rotatedRect.angle;
            if (angle < -45) {
                angle += 90;
            }

            // Don't rotate if angle == 0
            if (angle == 0) {
                // Just crop and return resulting Mat
                dst = dst.submat(rect);
                return dst;
            }

            dst = rotateMat(dst, angle, scale);
        }

        maxMatOfPoint = findCountours(dst);

        if (maxMatOfPoint == null) {
            return dst;
        }
        double area1 = rect.area();

        rect = Imgproc.boundingRect(maxMatOfPoint);
        // If new founding rect is not very smaller then first
        if (area1 / rect.area() < 10) {
            Core.rectangle(dst, rect.tl(), rect.br(), new Scalar(255, 0, 255, 255), 5);
            dst = dst.submat(rect);
        }

        return dst;
    }

    private static Mat changePerspective(List<Point> corners, Mat dst) {
        Point p1, p2, p3, p4;

        p1 = corners.get(0);
        p2 = corners.get(1);
        p3 = corners.get(2);
        p4 = corners.get(3);

        drawPoints(p1, p2, p3, p4);

        logger.log("p1 = " + p1);
        logger.log("p2 = " + p2);
        logger.log("p3 = " + p3);
        logger.log("p4 = " + p4);

        List<Point> target = new ArrayList<>(4);
        target.add(new Point(0, 0));
        target.add(new Point(dst.cols(), 0));
        target.add(new Point(dst.cols(), dst.rows()));
        target.add(new Point(0, dst.rows()));

        // compute the size of the card by keeping aspect ratio.
        double ratio = 1.6;
        double cardH = Math.sqrt((p3.x - p2.x) * (p3.x - p2.x) + (p3.y - p2.y) * ((p3.y - p2.y)));
        double cardW = ratio * cardH;

        Rect rect = new Rect((int) p1.x, (int) p1.y, (int) cardW, (int) cardH);

        target.clear();
        target.add(new Point(rect.x, rect.y));
        target.add(new Point(rect.x + rect.width, rect.y));
        target.add(new Point(rect.x + rect.width, rect.y + rect.height));
        target.add(new Point(rect.x, rect.y + rect.height));

        logger.log("rect = " + rect);
        logger.log(String.valueOf(target));

        Mat cornersMat = Converters.vector_Point2f_to_Mat(corners);
        Mat targetMat = Converters.vector_Point2f_to_Mat(target);
        Mat trans = Imgproc.getPerspectiveTransform(cornersMat, targetMat);
        int offset = 250;
        Mat proj = Mat.zeros(rect.height + offset, rect.width + offset, CvType.CV_8UC3);
//        Mat proj = new Mat();
        Imgproc.warpPerspective(dst, proj, trans, new Size(dst.cols(), dst.rows()));
//        Imgproc.warpPerspective(dst, proj, trans, proj.size());
//        Imgproc.warpPerspective(dst, proj, trans, new Size(3000, 3000));
//        Core.flip(proj, proj, 1);
        return proj;
    }

    private static List<Point> foundCorners(MatOfPoint2f maxMatOfPoint2f) {
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = Imgproc.arcLength(maxMatOfPoint2f, true) * 0.05;
        Imgproc.approxPolyDP(maxMatOfPoint2f, approxCurve,
                epsilon, true);

        Point[] rectArray = approxCurve.toArray();

        return sortPoints(rectArray);
    }

    private static List<Point> sortPoints(Point[] rectArray) {
        // Find the smallest point. (top-left)
        double smSum = Double.MAX_VALUE;
        int smIndx = 0;
        for (int i = 0; i < rectArray.length; i++) {
            Point point = rectArray[i];
            double pSum = point.x + point.y;
            if (pSum < smSum) {
                smSum = pSum;
                smIndx = i;
            }
        }

        List<Point> corners = new ArrayList<>(4);
        // Fill the list by points.
        // The first one would be smallest (top-left)
        // Others would be reordered be smallest ('cause Imgproc.approxPolyDP returned points by left-to-right.
        // But the Mat should be pointed be right-to-left
        int i = smIndx;
        do {
            if (i < 0) {
                i = rectArray.length - 1;
            }
            corners.add(rectArray[i]);
            i--;

        } while (i != smIndx);
        return corners;
    }

    private static void drawPoints(Point... points) {
        for (Point point : points) {
            Core.circle(img, point, 2, new Scalar(255, 255, 0), 2);
        }
        origWnd.setImage(img);
    }

    @Nullable
    private static MatOfPoint findCountours(Mat src) {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double maxArea = 0;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?

                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        /** Draw found contours **/
//        if (maxId >= 0) {
//            Imgproc.drawContours(src, contours, maxId, new Scalar(0, 255, 0,
//                    0), 8);
//        }

        if (maxId >= 0) {
            return contours.get(maxId);
        } else {
            return null;
        }
    }

    private static Mat rotateMat(Mat src, double angle, double scale) {
        Point center = new Point(src.cols() / 2, src.rows() / 2);
        Mat rotated = Imgproc.getRotationMatrix2D(center, angle, scale);
        Mat dst = new Mat();

        Imgproc.warpAffine(src, dst, rotated, dst.size());
        return dst;
    }

    private Point rotatePoint(Point p1, double angle) {
        int x = (int) ((p1.x * Math.cos(angle)) + (p1.y + Math.sin(angle)));
        int y = (int) ((p1.x * Math.sin(angle)) + (p1.y + Math.cos(angle)));
        return new Point(x, y);
    }

    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }
}
