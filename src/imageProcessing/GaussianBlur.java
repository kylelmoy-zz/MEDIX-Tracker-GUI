package imageProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

/**
 * Gaussian Blur implementation
 * @author Kyle moy
 *
 */
public class GaussianBlur {
	public static Kernel kernel;
	private static Kernel makeKernel(float radius) {
		if (kernel != null) return kernel;
		int r = (int) Math.ceil(radius);
		int rows = r * 2 + 1;
		float[] matrix = new float[rows];
		float sigma = radius / 3;
		float sigma22 = 2 * sigma * sigma;
		float sigmaPi2 = (float) (2 * Math.PI * sigma);
		float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
		float radius2 = radius * radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row * row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float) Math.exp(-(distance) / sigma22)
						/ sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;
		return new Kernel(rows, 1, matrix);
	}

	public static BufferedImage blur(BufferedImage src, float radius) {
		int width = src.getWidth();
		int height = src.getHeight();

		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		src.getRGB(0, 0, width, height, inPixels, 0, width);
		kernel = makeKernel(radius);
		convolveAndTranspose(kernel, inPixels, outPixels, width, height);
		convolveAndTranspose(kernel, outPixels, inPixels, height, width);

		dst.setRGB(0, 0, width, height, inPixels, 0, width);
		return dst;
	}

	private static void convolveAndTranspose(Kernel kernel, int[] inPixels,
			int[] outPixels, int width, int height) {
		float[] matrix = kernel.getKernelData(null);
		int cols = kernel.getWidth();
		int cols2 = cols / 2;

		for (int y = 0; y < height; y++) {
			int index = y;
			int ioffset = y * width;
			for (int x = 0; x < width; x++) {
				float r = 0, g = 0, b = 0, a = 0;
				int moffset = cols2;
				for (int col = -cols2; col <= cols2; col++) {
					float f = matrix[moffset + col];

					if (f != 0) {
						int ix = x + col;
						if (ix < 0) {
							ix = 0;
						} else if (ix >= width) {
							ix = width - 1;
						}
						int rgb = inPixels[ioffset + ix];
						a += f * ((rgb >> 24) & 0xff);
						r += f * ((rgb >> 16) & 0xff);
						g += f * ((rgb >> 8) & 0xff);
						b += f * (rgb & 0xff);
					}
				}
				int ia = 0xff;
				int ir = Math.min(255, (int) (r + 0.5));
				int ig = Math.min(255, (int) (g + 0.5));
				int ib = Math.min(255, (int) (b + 0.5));
				outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
				index += height;
			}
		}
	}
}
