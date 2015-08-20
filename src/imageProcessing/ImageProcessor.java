package imageProcessing;

import gui.Controller;
import gui.GUI;
import imageAcquisition.ImageProducer;
import imageProcessing.ImageTools.ImageEntry;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.MOVE_DECISION_BOUNDARY_PX;
import static dto.Properties.MOVE_DECISION_CONFIDENCE_DISTANCE;
import static dto.Properties.SEGMENTATION_COMPONENT_MIN_SIZE;
import static dto.Properties.SEGMENTATION_DELAY;
import static dto.Properties.SEGMENTATION_FAILURE_THRESHOLD;
import static dto.Properties.SEGMENTATION_THRESHOLD;
import static dto.Properties.SEGMENTATION_WINDOW_SIZE;

import java.nio.ByteBuffer;

public class ImageProcessor implements Runnable {
	private ImageProducer input;
	private long referenceTime = 0;
	private boolean[][] referenceImage;
	private double[] centroid = { IMAGE_WIDTH / 2, IMAGE_HEIGHT / 2 };
	private double[] prevCentroid = { IMAGE_WIDTH / 2, IMAGE_HEIGHT / 2 };
	private boolean confident = false;
	private boolean isNew = true;
	private int consecutiveConfidence = 0;
	private Thread thread;
	public boolean run = true;

	public ImageProcessor(ImageProducer in) {
		input = in;
		thread = new Thread(this);
	}

	public void start() {
		thread.start();
	}

	private static boolean[][] threshold(byte[] src) {

		// Convert image to grayscale. Could use only one color channel instead,
		// for speed.
		int[] gray = new int[src.length / 3];
		int j = 0;
		for (int i = 0; i < src.length; i += 3) {
			int r = src[i];
			int g = src[i + 1];
			int b = src[i + 2];
			gray[i / 3] = (int) ((r + g + b) / 3);
		}

		// Calculate integral image
		int[][] integral = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
		for (j = 0; j < IMAGE_HEIGHT; j++) {
			for (int i = 0; i < IMAGE_WIDTH; i++) {
				// Maaaaath. Look up integral image, or summed area tables.
				// Basically a really fast way to calculate the average in an
				// area.
				integral[i][j] = sub(gray, i, j, IMAGE_WIDTH)
						+ sub(integral, i - 1, j) + sub(integral, i, j - 1)
						- sub(integral, i - 1, j - 1);
			}
		}

		// Calculate thresholded image
		boolean[][] threshold = new boolean[IMAGE_WIDTH][IMAGE_HEIGHT];
		for (j = 0; j < IMAGE_HEIGHT; j++) {
			for (int i = 0; i < IMAGE_WIDTH; i++) {
				// If the current pixel is less than SEGMENTATION_THRESHOLD
				// ratio, mark it as possibly the worm.
				if (sub(gray, i, j, IMAGE_WIDTH)
						/ average(integral, i, j, IMAGE_WIDTH, IMAGE_HEIGHT,
								SEGMENTATION_WINDOW_SIZE) < SEGMENTATION_THRESHOLD) {
					threshold[i][j] = true;
				} else {
					threshold[i][j] = false;
				}
			}
		}

		return threshold;
	}

	private static double average(int[][] integral, int x, int y, int w, int h,
			int winsize) {
		int x1 = x - 1 - winsize;
		int y1 = y - 1 - winsize;
		int x2 = x + winsize;
		int y2 = y + winsize;

		if (x2 > w - 1)
			x2 = w - 1;
		if (y2 > h - 1)
			y2 = h - 1;

		int a, b, c, d;
		if (x1 < 0 || y1 < 0)
			a = 0;
		else
			a = integral[x1][y1];
		if (y1 < 0)
			b = 0;
		else
			b = integral[x2][y1];
		if (x1 < 0)
			c = 0;
		else
			c = integral[x1][y2];
		d = integral[x2][y2];
		return (d - c - b + a) / (double) ((x2 - x1) * (y2 - y1));
	}

	private static int sub(int[] arr, int i, int j, int w) {
		if (i < 0)
			return 0;
		if (j < 0)
			return 0;
		int val = arr[i + (j * w)];
		if (val < 0)
			val += 255;
		return val;
	}

	private static int sub(int[][] pix, int i, int j) {
		if (i < 0 || j < 0)
			return 0;
		return pix[i][j];
	}

	public ByteBuffer overlayImage(ByteBuffer img) {
		byte[] wrap = new byte[img.remaining()];
		img.get(wrap);
		byte[] red = { 0, 0, (byte) 255 };
		byte[] green = { 0, (byte) 255, 0 };
		byte[] blue = { (byte) 255, 0, 0 };
		for (int y = 0; y < IMAGE_HEIGHT; y++) {
			for (int x = 0; x < IMAGE_WIDTH; x++) {
				int ind = (y * IMAGE_WIDTH + x) * 3;
				if (x == (int) centroid[0] || y == (int) centroid[1]) {
					byte[] color = green;
					if (!confident)
						color = red;
					wrap[ind] = color[0];
					wrap[ind + 1] = color[1];
					wrap[ind + 2] = color[2];
				}
				if (x == MOVE_DECISION_BOUNDARY_PX
						|| x == IMAGE_WIDTH - MOVE_DECISION_BOUNDARY_PX) {
					wrap[ind] = (byte) 255;
					wrap[ind + 1] = (byte) 0;
					wrap[ind + 2] = (byte) 0;
				}
				if (y == MOVE_DECISION_BOUNDARY_PX
						|| y == IMAGE_HEIGHT - MOVE_DECISION_BOUNDARY_PX) {
					wrap[ind] = (byte) 255;
					wrap[ind + 1] = (byte) 0;
					wrap[ind + 2] = (byte) 0;
				}
			}
		}
		return ByteBuffer.wrap(wrap);
	}

	public double[] getCentroid() {
		if (isNew) {
			isNew = false;
			return centroid;
		} else {
			return null;
		}
	}

	public void getNewReference() {
		ImageEntry entry = input.peek();
		if (entry == null) {
			return;
		}
		byte[] wrap;
		synchronized (entry) {
			ByteBuffer img = entry.img;
			wrap = new byte[img.remaining()];
			img.get(wrap);
			img.rewind();
		}
		referenceImage = threshold(wrap);
	}

	/**
	 * Counts the number of connected components in a boolean mask using
	 * UnionFind
	 * 
	 * @param image
	 *            The boolean mask
	 * @return The number of connected components
	 * @throws ComponentLabellingFailureException
	 */
	private static double[] largestComponent(boolean[][] image)
			throws SegmentationFailureException {
		Component cmp = new Component(image);
		// cmp.count();
		int w = image.length;
		int h = image[0].length;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if (image[j][i]) {
					boolean done = false;
					for (int x = -1; x < 2; x++) {
						for (int y = -1; y < 2; y++) {
							final int _x = j + x;
							final int _y = i + y;
							if (_x < 0 || _x >= w)
								continue;
							if (_y < 0 || _y >= h)
								continue;
							if (image[_x][_y]) {
								cmp.union(j, i, _x, _y);
								done = true;
							}
						}
					}
					if (!done) {
						image[j][i] = false;
						cmp.union(j, i, 0, 0);
					}
				}
			}
		}
		return cmp.largestComponentCentroid();
	}

	/**
	 * Double check movement location with worm location...
	 * 
	 * @return If we're confident that the thing that moved is probably the worm
	 */
	public boolean isConfident() {
		double[] realCentroid;
		try {
			realCentroid = largestComponent(referenceImage);
		} catch (SegmentationFailureException e) {
			return false;
		}
		boolean realDistance = Math.abs(Math.sqrt(Math.pow(realCentroid[0]
				- centroid[0], 2)
				+ Math.pow(realCentroid[1] - centroid[1], 2))) < MOVE_DECISION_CONFIDENCE_DISTANCE;
		boolean consecutiveDistance = Math.abs(Math.sqrt(Math.pow(
				prevCentroid[0] - centroid[0], 2)
				+ Math.pow(prevCentroid[1] - centroid[1], 2))) < MOVE_DECISION_CONFIDENCE_DISTANCE;
		if (consecutiveDistance) consecutiveConfidence ++;
		else consecutiveConfidence = 0;
		confident = consecutiveConfidence >= 3;
		return confident;
	}

	/**
	 * The <code>Component</code> class provides utilities to use UnionFind
	 * 
	 * @author Kyle Moy
	 *
	 */
	private static class Component {
		private final int[] id;
		private int[] segment;
		private int count;
		private int w;
		private int h;

		public Component(boolean[][] image) throws SegmentationFailureException {
			w = image.length;
			h = image[0].length;
			id = new int[w * h + 1];
			count = 0;
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					int p = j + (i * w);
					if (image[j][i]) {
						id[p] = p + 1;
						count++;
					} else
						id[p] = 0;
				}
			}
			if (count == 0) {
				throw new SegmentationFailureException("No components found!");
			}
			segment = new int[w * h + 1];
		}

		public int count() {
			return count;
		}

		public boolean connected(final int p, final int q) {
			return id[p] == id[q];
		}

		public void union(int x1, int y1, int x2, int y2) {
			int p = x1 + (y1 * w);
			int q = x2 + (y2 * w);
			if (p < 0 || q < 0)
				return;
			if (connected(p, q))
				return;
			final int pid = id[p];
			// segment[p] = true;
			for (int i = 0; i < id.length; i++)
				if (id[i] == pid)
					id[i] = id[q];
			count--;
		}

		public double[] largestComponentCentroid()
				throws SegmentationFailureException {
			if (count == 0) {
				// No Components!
				throw new SegmentationFailureException("No components found!");
			}
			int largestId = -1;
			int largest = 0;
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					int compId = id[j + (i * w)];
					if (compId != 0)
						++segment[compId];
				}
			}
			for (int i = 0; i < segment.length; i++) {
				if (segment[i] > largest) {
					largest = segment[i];
					largestId = i;
				}
			}
			if (largest < SEGMENTATION_COMPONENT_MIN_SIZE) {
				throw new SegmentationFailureException("Components not large enough!");
			}
			int avgx = 0;
			int avgy = 0;
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					if (id[j + (i * w)] == largestId) {
						avgx += j;
						avgy += i;
					}
				}
			}
			return new double[] { ((double) avgx / (double) largest),
					((double) avgy / (double) largest) };
		}
	}

	@SuppressWarnings("serial")
	private static class SegmentationFailureException extends Exception {
		public SegmentationFailureException(String message) {
			super(message);
		}
	}

	@Override
	public void run() {
		long lastSuccess = System.currentTimeMillis();
		while (run) {
			try {
				if (System.currentTimeMillis() - referenceTime > SEGMENTATION_DELAY) {
					ImageEntry entry = input.peek();
					if (entry == null) {
						// Thread just started, no images to peek! Wait a bit.
						Thread.sleep(500);
						continue;
					}
					byte[] wrap;
					synchronized (entry) {
						ByteBuffer img = entry.img;
						wrap = new byte[img.remaining()];
						img.get(wrap);
						img.rewind();
					}
					boolean[][] seg = threshold(wrap);
					if (referenceImage != null) {
						// Calculate Difference Image
						boolean[][] dif = new boolean[seg.length][seg[0].length];
						int difCount = 0;
						for (int y = 0; y < IMAGE_HEIGHT; y++) {
							for (int x = 0; x < IMAGE_WIDTH; x++) {
								if (seg[x][y] && !referenceImage[x][y]) {
									dif[x][y] = true;
									difCount++;
								}
							}
						}
						referenceImage = seg;
						referenceTime = System.currentTimeMillis();

						// If the image is too different... What happened?
						if (difCount < 600) {
							try {
								prevCentroid = centroid;
								centroid = largestComponent(dif);
								isNew = true;
								lastSuccess = System.currentTimeMillis();
							} catch (SegmentationFailureException e) {
								//System.out.println("No components.");
							}
						}
						if (System.currentTimeMillis() - lastSuccess > SEGMENTATION_FAILURE_THRESHOLD) {
							//Return to default just in case.
							run = false;
							centroid[0] = IMAGE_WIDTH / 2;
							centroid[1] = IMAGE_HEIGHT / 2;
							isNew = true;
							Controller.alert("Tracking Stopped", "Tracking stopped", "The worm could not be located.");
							GUI.getController().stop();
							break;
						}
					} else {
						referenceImage = seg;
						referenceTime = System.currentTimeMillis();
						//centroid = largestComponent(seg);
					}
				} else {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
