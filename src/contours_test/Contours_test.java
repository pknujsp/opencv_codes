package contours_test;

import java.awt.List;
import java.util.ArrayList;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Contours_test
{

	public static void main(String[] args)
	{
		final String datapath = "E:/EclipseProject/opencv_test/sample_coin.jpg";
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat image_original = Imgcodecs.imread(datapath, 4);
		preprocess_phase ppe = new preprocess_phase();
		processing pg = new processing();

		Mat processed_image = ppe.pre_process(datapath);

		// Mat image = pg.make_circle(pg.get_contours(processed_image), image_original);
		Mat image = pg.houghtransform(processed_image, image_original);

		HighGui.imshow("coindetected", image);
		HighGui.resizeWindow("coindetected", 1280, 720);
		HighGui.imshow("processed_image", processed_image);
		HighGui.resizeWindow("processed_image", 1280, 720);
		HighGui.waitKey();
	}

}

class preprocess_phase
{
	Mat pre_process(String imagepath)
	{
		
		String filename = imagepath;
		Mat OriginalImage = Imgcodecs.imread(filename, 4);

		Mat mat1 = new Mat();

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

		Imgproc.morphologyEx(OriginalImage, mat1, Imgproc.MORPH_ELLIPSE, element);

		Mat mat2 = new Mat();

		
		
		Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.adaptiveThreshold(mat1, mat2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 49,
				2);

	

		java.util.List<MatOfPoint> contours = new ArrayList<>();
		
		Mat hierarchy = new Mat();

		Imgproc.findContours(mat2, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		Object[] values = contours.toArray();

		Mat markers = Mat.zeros(mat2.size(), CvType.CV_8U);

		for (int i = 0; i < contours.size(); i++)
		{
			Point center = new Point();
			float[] radius = new float[1];
			MatOfPoint2f points = new MatOfPoint2f(((MatOfPoint) values[i]).toArray());
			Imgproc.minEnclosingCircle(points, center, radius);

			if (radius[0] > 70 && radius[0] < 400)
				Imgproc.circle(markers, center, (int) radius[0], new Scalar(255), 8);
		}

		return markers;

	}

}

class processing
{
	ArrayList<RotatedRect> get_contours(Mat image)
	{
		java.util.List<MatOfPoint> contours = new ArrayList<>();
		ArrayList<RotatedRect> circles = new ArrayList<>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Object[] contours_array = contours.toArray();
		for (int i = 0; i < contours.size(); i++)
		{
			MatOfPoint2f point = new MatOfPoint2f(((MatOfPoint) contours_array[i]).toArray());
			RotatedRect value = Imgproc.minAreaRect(point);
			value.angle = (value.size.width + value.size.height) / 4f;
			if (value.angle > 70 && value.angle < 400)
			{
				circles.add(value);
			}
		}
		return circles;
	}

	Mat make_circle(ArrayList<RotatedRect> data, Mat given_image)
	{
		Object[] rectdata = data.toArray();
		Mat image = given_image;
		for (int i = 0; i < data.size(); i++)
		{
			double radius = ((RotatedRect) rectdata[i]).angle;
			String string = "C " + i + ":" + (int) radius;
			Imgproc.circle(image, ((RotatedRect) rectdata[i]).center, (int) radius, new Scalar(0, 255, 0), 10);
			Imgproc.putText(image, string, ((RotatedRect) rectdata[i]).center, 1, 10, new Scalar(255), 6);
		}
		return image;
	}

	Mat houghtransform(Mat processed_data, Mat originaldata)
	{
		Mat givendata = processed_data;
		Mat original = originaldata;
		Mat circles = new Mat();
		Imgproc.HoughCircles(givendata, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 90, 115, 30, 70, 400);

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

				Imgproc.circle(original, center, radius, new Scalar(0, 255, 255), 20);
				Imgproc.putText(original, string, center, 1, 11, new Scalar(255), 6);
				System.out.println(radius);
			}

		}
		return original;
	}

}

class coin_num
{
	int coin_10_1;
	int coin_10_2;
	int coin_50;
	int coin_100;
	int coin_500;

	byte coin_number()
	{
		byte number;
		number = (byte) (coin_10_1 + coin_10_2 + coin_50 + coin_100 + coin_500);
		return number;
	}

	void coinnum_10_1()
	{
		this.coin_10_1++;
	}

	void coinnum_10_2()
	{
		this.coin_10_2++;
	}

	void coinnum_50()
	{
		this.coin_50++;
	}

	void coinnum_100()
	{
		this.coin_100++;
	}

	void coinnum_500()
	{
		this.coin_500++;
	}
}
