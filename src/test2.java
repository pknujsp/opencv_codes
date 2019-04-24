
import java.util.ArrayList;
import java.util.List;

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

public class test2
{

	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		startcalc("sampleimage11.png");
	}

	static void startcalc(String imagefile)
	{
		String datapath = "E:/EclipseProject/opencv_test/" + imagefile;
		Mat image = new Mat();
		GetA4image getA4image = new GetA4image();
		// removenoise_class removenoise = new removenoise_class();
		Mat image_original = Imgcodecs.imread(datapath, 4);
		System.out.println(image_original.width() + "   " + image_original.height());
		Mat cropped_image = getA4image.GetCALCULATED_IMAGE(image_original);

		preprocess_image ppe = new preprocess_image();
		processing_image pg = new processing_image();
		processing_hough phh = new processing_hough();

		Mat processed_image = ppe.pre_process(cropped_image);

		// Mat clustering_mat = removenoise.clustering(cropped_image);
		// Mat testmat = new Mat();
		// Imgproc.cvtColor(clustering_mat, testmat, Imgproc.COLOR_RGB2GRAY);

		// Mat kmeansmat = ppe.pre_process_kmeans(testmat);
		// image = pg.make_circle(pg.get_contours(processed_image), cropped_image);
		image = phh.houghtransform(processed_image, cropped_image);
		// HighGui.imshow("image_original", image_original);

		HighGui.imshow("binaryimage", ppe.binary_image);
		// HighGui.imshow("processed_mat", ppe.processed_mat);
		// HighGui.imshow("testmat", testmat);
		HighGui.imshow("image" + imagefile, image);
		HighGui.resizeWindow("binaryimage", 1920, 1080);
		// HighGui.resizeWindow("processed_mat", 1920, 1080);
		HighGui.resizeWindow("image" + imagefile, 1920, 1080);
		HighGui.waitKey();
		// System.exit(0);
	}
}

class processing_image
{
	ArrayList<RotatedRect> get_contours(Mat image)
	{
		java.util.List<MatOfPoint> contours = new ArrayList<>();
		ArrayList<RotatedRect> circles = new ArrayList<>();
		Mat hierarchy = new Mat();
		RotatedRect value = new RotatedRect();

		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Object[] contours_array = contours.toArray();
		for (int i = 0; i < contours.size(); i++)
		{
			MatOfPoint2f point = new MatOfPoint2f(((MatOfPoint) contours_array[i]).toArray());

			value = Imgproc.minAreaRect(point);
			value.angle = (value.size.width + value.size.height) / 4f;
			if (value.angle > 45 && value.angle < 90)
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
			String puttextval = String.valueOf((int) Math.floor(radius));

			Imgproc.circle(image, ((RotatedRect) rectdata[i]).center, (int) radius, new Scalar(255, 0, 0), 2);
			Imgproc.putText(image, puttextval,
					new Point(((RotatedRect) rectdata[i]).center.x - 21, ((RotatedRect) rectdata[i]).center.y + 10),
					Core.FONT_ITALIC, 1, new Scalar(0, 255, 255), 2);
		}
		return image;
	}
}

class preprocess_image
{
	Mat mattodisplay;
	Point[] distance_center;
	int[] distance_radius;
	Mat binary_image = new Mat();
	Mat processed_mat = new Mat();

	Mat pre_process(Mat givendata)
	{

		Mat OriginalImage = givendata;

		Mat mat1 = new Mat();

		Mat kernel = Mat.ones(new Size(3, 3), CvType.CV_8U);

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

		Imgproc.morphologyEx(OriginalImage, mat1, Imgproc.MORPH_ELLIPSE, element);

		Imgproc.GaussianBlur(mat1, mat1, new Size(5, 5), 3);

		Mat mat2 = new Mat();

		Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY);

		Imgproc.adaptiveThreshold(mat1, mat2, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 49, 3);

		Imgproc.erode(mat2, mat2, kernel, new Point(2, 2), 1);

		binary_image = mat2;

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

			if (radius[0] > 45 && radius[0] < 80)
				Imgproc.circle(markers, center, (int) radius[0], new Scalar(255), 2);
		}

		processed_mat = markers.clone();

		return markers;

	}

	Mat pre_process_kmeans(Mat givendata)
	{
		Mat mat2 = givendata.clone();

		binary_image = givendata;

		java.util.List<MatOfPoint> contours = new ArrayList<>();

		Mat hierarchy = new Mat();

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		Mat kernel = Mat.ones(new Size(3, 3), CvType.CV_8U);

		Imgproc.morphologyEx(mat2, mat2, Imgproc.MORPH_ELLIPSE, element);

		Imgproc.dilate(mat2, mat2, kernel, new Point(2, 2), 1);

		Imgproc.GaussianBlur(mat2, mat2, new Size(1, 1), 1);

		Imgproc.threshold(mat2, mat2, 0, 255, Imgproc.THRESH_BINARY_INV);

		HighGui.imshow("mat2", mat2);

		Imgproc.findContours(mat2, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Object[] values = contours.toArray();

		Mat markers = Mat.zeros(mat2.size(), CvType.CV_8U);

		for (int i = 0; i < contours.size(); i++)
		{
			Point center = new Point();
			float[] radius = new float[1];
			MatOfPoint2f points = new MatOfPoint2f(((MatOfPoint) values[i]).toArray());
			Imgproc.minEnclosingCircle(points, center, radius);

			if (radius[0] > 34 && radius[0] < 60)
				Imgproc.circle(markers, center, (int) radius[0], new Scalar(255), 2);
		}
		HighGui.imshow("markers", markers);
		return markers;

	}

}

class processing_hough
{

	Mat houghtransform(Mat processed_data, Mat originaldata)
	{
		Mat givendata = processed_data;
		Mat original = originaldata;
		Mat circles = new Mat();
		Imgproc.HoughCircles(givendata, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 95, 110, 30, 42, 84);

		if (circles.cols() > 0)
		{
			for (int x = 0; x < circles.total(); x++)
			{
				double circleDATA[] = circles.get(0, x);
				if (circleDATA == null)
				{
					break;
				}
				Point center = new Point((int) circleDATA[0], (int) circleDATA[1]);
				int radius = (int) circleDATA[2];

				String string = "" + radius;

				Imgproc.circle(original, center, radius, new Scalar(255, 0, 0), 2);
				Imgproc.putText(original, string, new Point(center.x - 25, center.y + 10), Core.FONT_ITALIC, 1.1,
						new Scalar(0, 255, 255), 2);
				System.out.println(radius);
			}

		}
		return original;
	}

}
