

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

public class MRFFileInputStream {
	GZIPInputStream is;
	public MRFFileInputStream (File file) throws FileNotFoundException, IOException {
		is = new GZIPInputStream(new FileInputStream(file));
	}
	
	public ImageEntry readImageEntry() throws IOException {
		byte[] buff = new byte[IMAGE_WIDTH * IMAGE_HEIGHT];
		int f = 0;
		long timeStamp = readLong(is);
		int x = readInt(is);
		int y = readInt(is);
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (timeStamp));
		System.out.println(date + "\t" + x + "\t" + y);
		for (int i = 0; i < buff.length; i++) {
			buff[i] = (byte)is.read();
			//if (buff[i] == 0) buff[i] = (byte)255;
		}
		ByteBuffer img = ByteBuffer.wrap(buff);
		System.out.println(buff.length);
		return new ImageEntry(img,timeStamp,new int[]{x,y});
	}
	
	public void finish() throws IOException {
		is.close();
	}
	
	public boolean hasNext() throws IOException {
		return is.available() >= 1;
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
