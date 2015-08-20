package imageRecording;
import imageProcessing.ImageTools;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

public class MRFtoImages {
	public static void main(String[] args) throws Exception {
		File mrf = new File("R:/testrec.mrf");
		GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(mrf));
		byte[] buff = new byte[IMAGE_WIDTH * IMAGE_HEIGHT];
		byte[] longs = new byte[8];
		int f = 0;
		while (gzis.available() != 0) {
			//long timeStamp = readLong(gzis);
			//int x = readInt(gzis);
			//int y = readInt(gzis);
			//String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (timeStamp));
			//if (f % 10 == 0) System.out.println(date + "\t" + x + "\t" + y);
			
			for (int i = 0; i < buff.length; i++) {
				buff[i] = (byte)gzis.read();
				if (buff[i] == 0) buff[i] = (byte)255;
			}
			
			BufferedImage img = ImageTools.toBufferedImage(ByteBuffer.wrap(buff),BufferedImage.TYPE_BYTE_GRAY);
			ImageIO.write(img, "jpeg", new File("R:/test/" + f++ + ".jpg"));
			//if (f > 10) break;
		}
	}
	private static int readInt(GZIPInputStream is) {
		byte[] buff = new byte[4];
		try {
			is.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BigInteger(buff).intValue();
	}
	private static long readLong(GZIPInputStream is) {
		byte[] buff = new byte[8];
		try {
			is.read(buff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BigInteger(buff).longValue();
	}
}
