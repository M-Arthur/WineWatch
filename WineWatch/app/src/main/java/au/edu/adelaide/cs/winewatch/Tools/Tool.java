package au.edu.adelaide.cs.winewatch.Tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * Created by Arthur on 17/05/15.
 */
public class Tool {
	public static int[] getColor(double[] temperature) {
		// TODO: Change color according to the temperature
		// 0x is the sign of integer, ff represents the transparent degree, 00ff00 stands for RGB values.
		// The fermentation temperature is generally 15 degree to 35 degree.

		int[] color = new int[temperature.length];
		double tempT = 0;
		int tempC = 0;
		int transparent = 180;

		for (int i = 0; i < temperature.length; i++) {
			tempT = temperature[i];

			if (tempT <= 15) {
				color[i] = Color.argb(transparent, 0, 0, 255);
			} else if (tempT <= 23) {
				tempT -= 15;
				tempC = (int) tempT * 31;
				color[i] = Color.argb(transparent, 0, tempC, 255);
			} else if (tempT <= 25) {
				tempT -= 23;
				tempC = (int) tempT * 125;
				color[i] = Color.argb(transparent, 0, 255, 255 - tempC);
			} else if (tempT <= 27) {
				tempT -= 25;
				tempC = (int) tempT * 125;
				color[i] = Color.argb(transparent, tempC, 255, 0);
			} else if (tempT <= 35) {
				tempT -= 27;
				tempC = (int) tempT * 31;
				color[i] = Color.argb(transparent, 255, 255 - tempC, 0);
			} else if (tempT > 35) {
				color[i] = Color.argb(transparent, 255, 0, 0);
			}
		}
		return color;
	}

	public static int getColor(double temperature) {
		// TODO: Change color according to the temperature
		// 0x is the sign of integer, ff represents the transparent degree, 00ff00 stands for RGB values.
		// The fermentation temperature is generally 15 degree to 35 degree.
		int color = Color.RED;
		double tempT = temperature;
		int tempC = 0;
		int transparent = 180;


		if (tempT <= 15) {
			color = Color.argb(transparent, 0, 0, 255);
		} else if (tempT <= 23) {
			tempT -= 15;
			tempC = (int) tempT * 31;
			color = Color.argb(transparent, 0, tempC, 255);
		} else if (tempT <= 25) {
			tempT -= 23;
			tempC = (int) tempT * 125;
			color = Color.argb(transparent, 0, 255, 255 - tempC);
		} else if (tempT <= 27) {
			tempT -= 25;
			tempC = (int) tempT * 125;
			color = Color.argb(transparent, tempC, 255, 0);
		} else if (tempT <= 35) {
			tempT -= 27;
			tempC = (int) tempT * 31;
			color = Color.argb(transparent, 255, 255 - tempC, 0);
		} else if (tempT > 35) {
			color = Color.argb(transparent, 255, 0, 0);
		}
		return color;
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	                                                     int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
}
