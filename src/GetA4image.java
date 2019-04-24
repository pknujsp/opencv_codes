import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;

public class GetA4image
{
	Mat original_image = null;

	public Mat GetCALCULATED_IMAGE(Mat givenmat)
	{

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		original_image = givenmat;
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Mat binaryimage = new Mat();
		Mat gray_image = new Mat();

		Mat finalmat = new Mat();
		Mat kernel = Mat.ones(new Size(3, 3), CvType.CV_8U);

		Core.copyMakeBorder(original_image, gray_image, 4, 4, 4, 4, Core.BORDER_CONSTANT);
		original_image.convertTo(original_image, -1, 1.1, 0.3);
		Imgproc.cvtColor(original_image, gray_image, Imgproc.COLOR_RGB2GRAY);

		Imgproc.GaussianBlur(gray_image, gray_image, new Size(13, 13), 6);

		Imgproc.adaptiveThreshold(gray_image, binaryimage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
				Imgproc.THRESH_BINARY, 29, 6);

		Imgproc.erode(binaryimage, binaryimage, kernel, new Point(2, 2), 4);
		Imgproc.dilate(binaryimage, binaryimage, kernel, new Point(2, 2), 3);

		Imgproc.findContours(binaryimage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat harrismat = Mat.zeros(binaryimage.size(), CvType.CV_8UC1);

		int largest_contour_index = 0;
		int largest_area = 0;

		Object[] contours_array = contours.toArray();
		double[] contour_index = new double[contours.size()];
		for (int i = 0; i < contours.size(); i++)
		{
			double a = Imgproc.contourArea((Mat) contours_array[i], false);
			contour_index[i] = a;
			if (a > largest_area)
			{
				largest_area = (int) a;
				largest_contour_index = i;
			}
		}

		Imgproc.drawContours(harrismat, contours, largest_contour_index, new Scalar(255), 10);

		Imgproc.morphologyEx(harrismat, harrismat, Imgproc.MORPH_DILATE, kernel, new Point(2, 2), 3);

		MatOfPoint corners = new MatOfPoint();
		int corner_count = 10;

		Imgproc.goodFeaturesToTrack(gray_image, corners, corner_count, 0.04, 90, new Mat(), 9, true, 0.06);

		Mat goodto = original_image.clone();

		Point[] points = corners.toArray();

		for (int i = 0; i < points.length; i++)
		{
			String cornerval = String.valueOf(points[i].x) + " " + String.valueOf(points[i].y);
			Imgproc.circle(goodto, points[i], 40, new Scalar(255), 8);
			// Imgproc.putText(goodto, cornerval, points[i], Core.FONT_ITALIC, 2.5, new
			// Scalar(255), 5);
		}

		Point[] points_4 = CalcPoints(points);

		finalmat = ResizeMat(WarpingImage(points_4));

		return finalmat;
	}

	public Point[] CalcPoints(Point[] points) // 사이 거리가 가장 먼 두 포인트를 계산
	{
		double[] y = new double[points.length];

		double FirstPointindex = 0.0;
		double FarPointindex = 0.0;
		double SecPointindex = 0.0;
		double FourthPointindex = 0.0;

		Point FirstPoint = new Point(0, 0); // 원점과 가장 가까운 포인트
		Point FarPoint = new Point(0, 0); // FirstPoint에서 가장 거리가 먼 포인트
		Point SecPoint = new Point(0, 0); // 두 번째 포인트
		Point FourthPoint = new Point(0, 0);

		for (int i = 0; i < points.length; i++)
		{
			y[i] = points[i].y;
		}

		Arrays.sort(y);
		FirstPoint.y = y[0];

		for (int i = 0; i < points.length; i++)
		{
			if (FirstPoint.y == points[i].y)
			{
				FirstPointindex = i;
				FirstPoint.x = points[i].x;
			}
		}

		double longest_dist = 0.0;
		double second_dist = 0.0;
		for (int i = 0; i < points.length; i++)
		{
			double distance = Math
					.sqrt(Math.pow(FirstPoint.x - points[i].x, 2) + Math.pow(FirstPoint.y - points[i].y, 2));
			if (FirstPoint.y != points[i].y)
			{
				System.out.println(distance + " " + points[i].x);
				if (distance > longest_dist)
				{
					second_dist = longest_dist;
					longest_dist = distance;
					FarPointindex = i;
					FarPoint.x = points[i].x;
					FarPoint.y = points[i].y;
				} else if (distance > second_dist && distance != longest_dist)
				{
					second_dist = distance;
					SecPointindex = i;
					SecPoint.x = points[i].x;
					SecPoint.y = points[i].y;
				}
			}
		}

		for (int i = 0; i < points.length; i++)
		{
			double distance = Math
					.sqrt(Math.pow(FirstPoint.x - points[i].x, 2) + Math.pow(FirstPoint.y - points[i].y, 2));
			if (distance == second_dist)
			{
				SecPointindex = i;
				SecPoint.x = points[i].x;
				SecPoint.y = points[i].y;
			}
		}

		double MaxWidth = 0.0;
		for (int i = 0; i < points.length; i++)
		{
			double x = points[i].x;
			double y1 = points[i].y;
			double width = Math
					.abs((FirstPoint.x * SecPoint.y + SecPoint.x * y1 + x * FirstPoint.y)
							- (SecPoint.x * FirstPoint.y + x * SecPoint.y + FirstPoint.x * y1))
					+ Math.abs((FirstPoint.x * y1 + x * FarPoint.y + FarPoint.x * FirstPoint.y)
							- (x * FirstPoint.y + FarPoint.x * y1 + FirstPoint.x * FarPoint.y))
					+ Math.abs((x * SecPoint.y + SecPoint.x * FarPoint.y + FarPoint.x * y1)
							- (SecPoint.x * y1 + FarPoint.x * SecPoint.y + x * FarPoint.y));
			if (width > MaxWidth)
			{
				MaxWidth = width;
				FourthPointindex = i;
				FourthPoint.x = points[i].x;
				FourthPoint.y = points[i].y;

			}
		}
		System.out.println("FirstPoint XY - " + FirstPoint.x + " " + FirstPoint.y);
		System.out.println("FarPoint XY - " + FarPoint.x + " " + FarPoint.y);
		System.out.println("SecPoint XY - " + SecPoint.x + " " + SecPoint.y);
		System.out.println("FourthPoint XY - " + FourthPoint.x + " " + FourthPoint.y);

		Point[] Points_4 = { FirstPoint, SecPoint, FarPoint, FourthPoint };
		return Points_4;
	}

	public Mat WarpingImage(Point[] points_4)
	{
		Point top1 = points_4[0];
		Point top2 = points_4[3];
		Point bottom1 = points_4[1];
		Point bottom2 = points_4[2];
		MatOfPoint2f a = null;
		MatOfPoint2f b = null;

		double width1, width2, height1, height2, maxwidth, maxheight;

		ArrayList<Point> rectangle = new ArrayList<Point>();
		rectangle.add(top1);
		rectangle.add(top2);
		rectangle.add(bottom1);
		rectangle.add(bottom2);

		width1 = Math.sqrt(Math.pow(bottom2.x - bottom1.x, 2) + Math.pow(bottom2.x - bottom1.x, 2));
		width2 = Math.sqrt(Math.pow(top2.x - top1.x, 2) + Math.pow(top2.x - top1.x, 2));

		height1 = Math.sqrt(Math.pow(top2.y - bottom2.y, 2) + Math.pow(top2.y - bottom2.y, 2));
		height2 = Math.sqrt(Math.pow(top1.y - bottom1.y, 2) + Math.pow(top1.y - bottom1.y, 2));

		maxwidth = (width1 < width2) ? width1 : width2;
		maxheight = (height1 < height2) ? height1 : height2;

		a = new MatOfPoint2f(top1, top2, bottom2, bottom1);
		b = new MatOfPoint2f(new Point(0, 0), new Point(maxwidth - 1, 0), new Point(maxwidth - 1, maxheight - 1),
				new Point(0, maxheight - 1));

		Mat WARPMAT = Imgproc.getPerspectiveTransform(a, b);
		Mat FINALMAT = new Mat();
		Imgproc.warpPerspective(original_image, FINALMAT, WARPMAT, new Size(maxwidth, maxheight));

		return FINALMAT;
	}

	public Mat ResizeMat(Mat mat)
	{
		Mat resized_mat = new Mat();
		Imgproc.resize(mat, resized_mat, new Size(1240,1754));
		return resized_mat;
	}
}
