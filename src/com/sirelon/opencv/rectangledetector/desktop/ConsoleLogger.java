package com.sirelon.opencv.rectangledetector.desktop;

import com.sirelon.opencv.rectangledetector.core.ILogger;

/**
 * Created by Sirelon on 04/05/2017.
 */
public class ConsoleLogger implements ILogger {

    @Override
    public void log(String log) {
        System.out.println(log);
    }

}
