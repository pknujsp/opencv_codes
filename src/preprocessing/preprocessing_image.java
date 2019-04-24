package preprocessing;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class preprocessing_image
{

	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat image = Imgcodecs.imread("E:/EclipseProject/opencv_test/sample_coin10.png", 4);

		Mat gray = new Mat();

		Mat mat2 = new Mat();

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

		Imgproc.bilateralFilter(image, gray, 6, 9, 7);

		Core.inRange(gray, new Scalar(0, 0, 0), new Scalar(255, 255, 255), mat2);

		// Imgproc.adaptiveThreshold(mat2, mat2, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
		// Imgproc.THRESH_BINARY_INV, 85, 3);

		// Imgproc.erode(mat2, mat2, kernel, new Point(3, 3), 2);
		// Imgproc.dilate(mat2, mat2, kernel, new Point(3, 3), 2);

		HighGui.imshow("mat2", mat2);
		HighGui.resizeWindow("mat2", 1280, 720);

		HighGui.waitKey();

	}

}
