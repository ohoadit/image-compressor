import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math;

public class KMeans {
	static int maximumIterations = 100;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: Kmeans <input-image> <k> <output-image>");
			return;
		}
		try {
			BufferedImage originalImage = ImageIO.read(new File(args[0]));
			int k = Integer.parseInt(args[1]);
			BufferedImage kmeansJpg = kmeans_helper(originalImage, k);
			ImageIO.write(kmeansJpg, "jpg", new File(args[2]));

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static BufferedImage kmeans_helper(BufferedImage originalImage, int k) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		BufferedImage kmeansImage = new BufferedImage(w, h, originalImage.getType());
		Graphics2D g = kmeansImage.createGraphics();
		g.drawImage(originalImage, 0, 0, w, h, null);
		// Read rgb values from the image
		int[] rgb = new int[w * h];
		int count = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				rgb[count++] = kmeansImage.getRGB(i, j);
			}
		}
		// Call kmeans algorithm: update the rgb values
		kmeans(rgb, k);

		// Write the new rgb values to the image
		count = 0;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				kmeansImage.setRGB(i, j, rgb[count++]);
			}
		}
		return kmeansImage;
	}

	// Your k-means code goes here
	// Update the array rgb by assigning each entry in the rgb array to its cluster
	// center
	private static void kmeans(int[] rgb, int k) {
		int[][] centroids = getInitialCentroids(k, rgb);
		HashMap<Integer, ArrayList<Integer>> clusterMap = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < centroids.length; i++) {
			clusterMap.put(i, new ArrayList<>());
		}
		int iterations = 0;
		while (true) {
			for (int i = 0; i < rgb.length; i++) {
				int[] rgbValue = getRGBValue(rgb[i]);
				int minDistance = calculateEuclideanDistance(rgbValue, centroids[0]);
				int clusterIndex = 0;
				for (int j = 1; j < centroids.length; j++) {
					int distance = calculateEuclideanDistance(rgbValue, centroids[j]);
					if (distance < minDistance) {
						minDistance = distance;
						clusterIndex = j;
					}
				}
				clusterMap.get(clusterIndex).add(Integer.valueOf(i));
			}
			iterations++;
			if (iterations == maximumIterations) {
				break;
			}
			centroids = calculateNewCentroids(rgb, centroids, clusterMap);
		}
		updatePixelValues(rgb, centroids, clusterMap);
		// System.out.println(Arrays.toString(getRGBValue(rgb[0])));
	}

	private static void updatePixelValues(int[] rgb, int[][] centroids,
			HashMap<Integer, ArrayList<Integer>> clusterMap) {
		for (Integer key : clusterMap.keySet()) {
			int centroidColor = getColorBit(centroids[key]);
			for (Integer index : clusterMap.get(key)) {
				rgb[index] = centroidColor;
			}
		}
	}

	private static int[][] getInitialCentroids(int k, int[] pixels) {
		int[][] centroids = new int[k][3];
		// int seed = 350;
		for (int i = 0; i < k; i++) {
			Random r = new Random();
			int randomlySelectedPixel = r.nextInt((pixels.length - 1) + 0) + 0; // 0 => minimum value
			System.out.println(randomlySelectedPixel);
			int[] rgbValue = getRGBValue(pixels[randomlySelectedPixel]);
			for (int j = 0; j < rgbValue.length; j++) {
				centroids[i][j] = rgbValue[j];
			}
			System.out.println(Arrays.toString(rgbValue));
			// seed += 15;
		}
		return centroids;
	}

	private static int[][] calculateNewCentroids(int[] rgb, int[][] centroids,
			HashMap<Integer, ArrayList<Integer>> clusterMap) {
		int[][] newCentroids = new int[centroids.length][3];
		for (Integer key : clusterMap.keySet()) {
			int red = 0, green = 0, blue = 0;
			for (Integer index : clusterMap.get(key)) {
				int[] pixelRGB = getRGBValue(rgb[index]);
				red += pixelRGB[0];
				green += pixelRGB[1];
				blue += pixelRGB[2];
			}
			int size = clusterMap.get(key).size();
			if (size == 0) {
				size = 1;
			}
			int[] centroidValue = { red / size, green / size, blue / size };
			newCentroids[key] = centroidValue;
		}
		return newCentroids;
	}

	private static int calculateEuclideanDistance(int[] pixelA, int[] pixelB) {
		int distance = 0;
		for (int i = 0; i < pixelA.length; i++) {
			distance += Math.pow((pixelA[i] - pixelB[i]), 2);
		}
		return (int) Math.sqrt(distance);
	}

	private static int[] getRGBValue(int color) {
		int[] rgb = new int[3];
		rgb[0] = (color & 0xff0000) >> 16; // Red color
		rgb[1] = (color & 0xff00) >> 8; // Green color
		rgb[2] = (color & 0xff); // Blue color
		return rgb;
	}

	private static int getColorBit(int[] rgb) {
		int color = (rgb[0]) << 16 | (rgb[1]) << 8 | (rgb[2]);
		return color;
	}
}
