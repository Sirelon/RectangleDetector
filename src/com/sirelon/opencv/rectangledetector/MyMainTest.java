package com.sirelon.opencv.rectangledetector;

import com.sirelon.opencv.rectangledetector.desktop.AwtView;
import com.sirelon.opencv.rectangledetector.desktop.ConsoleLogger;
import com.sirelon.opencv.rectangledetector.desktop.util.CVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
/**
 * Created by Sirelon on 28/04/2017.
 */
public class MyMainTest {

    public static void main(String[] args) {
        CVLoader.load();
//        img = Highgui.imread("data/imageRot.jpeg");
//        img = Highgui.imread("data/image.jpeg");
        Mat img = Highgui.imread("data/rectPerspHARD.jpg");
//        img = Highgui.imread("data/rectPersp.jpg");
//        img = Highgui.imread("data/test1.png");

        AwtView view = new AwtView();

        view.showSourceImage(img);

        RectangleDetector rectangleDetector = new RectangleDetector(new ConsoleLogger(), view);

        Mat proj = rectangleDetector.detectRect(img);
        if (proj != null) {
            view.showResultImage(proj);
        }
    }
}
