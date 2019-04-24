package compare_histogram;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class comparehisto
{
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
}
