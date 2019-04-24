package contours_test;

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
}
