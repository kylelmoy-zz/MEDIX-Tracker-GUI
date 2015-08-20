package imageProcessing;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;

import motorControl.MotorControl;

public class ImageTools {
	public static Image toJavaFXImage(ByteBuffer bytes) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(
					(RenderedImage) toBufferedImage(bytes), "png", out);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return new javafx.scene.image.Image(in);
	}

	public static BufferedImage toBufferedImage(ByteBuffer bytes) {
		BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		byte[] array = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		bytes.get(array);
		return img;
	}

	public static BufferedImage toBufferedImage(ByteBuffer bytes, int type) {
		BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, type);
		byte[] array = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		bytes.get(array);
		return img;
	}
	public static byte[] toGrayScale(byte[] img) {
		//By discarding 2 channels... Not averaging!
		byte[] gray = new byte[img.length/3];
		for(int i = 0; i < gray.length; i ++) {
			gray[i] = img[i*3];
			if (gray[i] > 200) gray[i] = 0;
		}
		return gray;
	}
	
	public static class ImageEntry {
		public ByteBuffer img;
		public long timeStamp;
		public int x, y;
		public int moving;
		
		public ImageEntry(ByteBuffer image) {
			img = image;
			timeStamp = System.currentTimeMillis();
			x = MotorControl.x();
			y = MotorControl.y();
			moving = MotorControl.isMoving();
		}
		
		
		//Debug constructor
		public ImageEntry (ByteBuffer image, long time, int[] pos) {
			img = image;
			timeStamp = time;
			x = pos[0];
			y = pos[1];
			moving = 0;
		}
	}
}
