package coindetect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.BadLocationException;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.ImageCapabilities;
import java.awt.geom.Point2D;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.lang.reflect.Array;

import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.Vec3f;
import com.sun.javafx.scene.traversal.Hueristic2D;
import com.sun.javafx.util.Utils;

public class circledetection
{

	public static void main(String[] args)
	{
		circledetection cdn = new circledetection();
		cdn.coin_detection();
	}

	ArrayList<Mat> make_coinimg(Mat src, Mat circles)
	{
		Scalar black_color = new Scalar(0);
		Scalar white_color = new Scalar(255);
		ArrayList<Mat> coins = new ArrayList<>();

		for (int x = 0; x < Math.min(circles.cols(), 100); x++)
		{
			Mat mask = null;
			Mat coin = null;
			Mat final_image = new Mat();
			double circleVec[] = circles.get(0, x);
			if (circleVec == null)
			{
				break;
			}
			Point center = new Point((int) circleVec[0], (int) circleVec[1]);
			int radius = (int) circleVec[2];

			double width = radius * 2.3;
			double height = radius * 2.3;
			Size size = new Size(width, height);
			mask = new Mat(size, CvType.CV_8UC1, black_color);
			coin = new Mat(size, CvType.CV_8UC1, black_color);

			Imgproc.circle(mask, new Point(mask.width() / 2, mask.height() / 2), radius, white_color, Core.FILLED);
			Imgproc.getRectSubPix(src, size, center, coin);

			Core.bitwise_and(coin, mask, final_image);

			coins.add(final_image);
		}

		return coins;
	}

	void coin_detection()
	{

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		int[] coin = new int[4];
		Mat image = Imgcodecs.imread("E:/EclipseProject/opencv_test/sample_coin5.png", 4);

		Mat input = new Mat();
		Mat circles = new Mat();
		Mat gray = new Mat();
		Mat mat_transformed = new Mat();
		Mat mat2 = new Mat();

		Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGRA2BGR);

		mat2 = gray.clone();

		Imgproc.bilateralFilter(gray, mat2, 11, 17, 10);

		Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_BGR2GRAY);

		Imgproc.equalizeHist(mat2, input);

		Imgproc.adaptiveThreshold(input, mat_transformed, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
				71, 8);

		Imgproc.morphologyEx(mat_transformed, mat_transformed, Imgproc.MORPH_DILATE, new Mat());

		Imgproc.HoughCircles(mat_transformed, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 150, 170, 41, 40, 240);

		int[] saveradius = new int[Math.min(circles.cols(), 100)];

		if (circles.cols() > 0)
		{
			for (int x = 0; x < Math.min(circles.cols(), 100); x++)
			{
				double circleVec[] = circles.get(0, x);
				if (circleVec == null)
				{
					break;
				}
				Point center = new Point((int) circleVec[0], (int) circleVec[1]);
				int radius = (int) circleVec[2];
				saveradius[x] = radius;
				String string = "R" + radius;
				Imgproc.circle(image, center, 30, new Scalar(0, 255, 255), 30);
				Imgproc.circle(image, center, radius, new Scalar(0, 255, 255), 30);
				Imgproc.putText(image, string, center, 1, 11, new Scalar(255), 6);
				System.out.println(radius);
			}
			
		}

		System.out.println("10원 : " + coin[0]);
		System.out.println("50원 : " + coin[1]);
		System.out.println("100원 : " + coin[2]);
		System.out.println("500원 : " + coin[3]);

		ArrayList<Mat> givendata = make_coinimg(input, circles);
		Object[] data = givendata.toArray();

		for (int i = 0; i < givendata.size(); i++)
		{
			HighGui.imshow("개별동전" + i, (Mat) data[i]);

			HighGui.imshow(i + "개별동전 히스토그램", drawhistogram((Mat) data[i]));

		}

		System.out.println(circles.total() + " " + givendata.size());

		HighGui.imshow("IMAGE", image);
		HighGui.resizeWindow("IMAGE", 1024, 720);
		HighGui.imshow("matt", mat_transformed);
		HighGui.resizeWindow("matt", 1024, 720);
		HighGui.waitKey();
	}

	Mat drawhistogram(Mat given_data)
	{
		Mat data_for_histo = given_data;
		int[][] datada = new int[data_for_histo.rows()][data_for_histo.cols()];

		double[] val = new double[0];
		for (int a = 0; a < data_for_histo.rows(); a++)
		{
			for (int b = 0; b < data_for_histo.cols(); b++)
			{

				val = data_for_histo.get(a, b);
				datada[a][b] = (int) val[0];
			}
		}

		double[] pointy = new double[256];

		System.out.println(data_for_histo.rows() + " " + data_for_histo.cols() + " " + data_for_histo.total() + " "
				+ data_for_histo.channels() + " " + (int) datada[200][131]);

		for (int a = 0; a < pointy.length; a++)
		{
			pointy[a] = 0;
		}

		for (int a = 0; a < data_for_histo.rows(); a++)
		{
			for (int b = 0; b < data_for_histo.cols(); b++)
			{
				int value = datada[a][b];
				pointy[value] += 1;
			}
		}

		Mat histogram = new Mat(new Size(256, 256), CvType.CV_8UC1, new Scalar(255));

		for (int i = 0; i < histogram.cols(); i++)
		{
			double startx = i;
			double endx = (i + 1);

			Point point1 = new Point(startx, 0);
			Point point2 = new Point(endx, pointy[i]);

			if (point2.y > 0)
				Imgproc.rectangle(histogram, point1, point2, new Scalar(0), -1);
		}
		Core.flip(histogram, histogram, 0);
		System.out.println(histogram.cols());
		return histogram;
	}



}
