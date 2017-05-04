package com.sirelon.opencv.rectangledetector.desktop;

import com.sirelon.opencv.rectangledetector.IView;
import com.sirelon.opencv.rectangledetector.desktop.util.ImgWindow;
import com.sun.istack.internal.Nullable;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Sirelon on 04/05/2017.
 */
public class AwtView implements IView {

    private final ImgWindow sourceWindow;
    private final ImgWindow resultWindow;

    @Nullable
    private Mat sourceMat;
    @Nullable
    private Mat resultMat;

    public AwtView() {
        sourceWindow = ImgWindow.newWindow();
        resultWindow = ImgWindow.newWindow();
    }

    @Override
    public void showSourceImage(Mat src) {
        sourceMat = src;
        showImage(sourceWindow, src);
    }

    @Override
    public void showResultImage(Mat dst) {
        resultMat = dst;
        showImage(resultWindow, dst);
    }

    @Override
    public void drawRectangle(Rect rect) {
        drawRectangle(resultMat, rect);
    }

    @Override
    public void drawCircles(Point... points) {
        if (sourceMat != null) {
            for (Point point : points) {
                Core.circle(sourceMat, point, 2, new Scalar(255, 255, 0), 2);
            }
            sourceWindow.setImage(sourceMat);
        }
    }

    @Override
    public void drawContours(List<MatOfPoint> contours, int maxId) {
        if (sourceMat != null && maxId >= 0)
            Imgproc.drawContours(sourceMat, contours, maxId, new Scalar(0, 255, 0,
                    0), 8);
    }

    private static void showImage(ImgWindow window, Mat image) {
        window.setImage(image);
    }

    private static void drawRectangle(@Nullable Mat mat, Rect rect) {
        if (mat != null)
            Core.rectangle(mat, rect.tl(), rect.br(), new Scalar(255, 0, 0, 255), 2);
    }
}
