package com.sirelon.opencv.rectangledetector.desktop.util;

import java.io.File;

public class CVLoader {
	public static void load() {
		System.load(new File("libs/libopencv_java248.dylib").getAbsolutePath());
	}
}
