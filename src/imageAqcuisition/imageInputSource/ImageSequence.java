package imageAqcuisition.imageInputSource;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

/**
 * An image source that gets images from a file directory.
 * 
 * @author Kyle Moy
 *
 */
public class ImageSequence implements ImageInputSource {
	File[] files;
	int seek;
	String filePath;
	boolean fancy = false;
	long timeReady = System.currentTimeMillis();
	int framesPerSecond = 60;

	/**
	 * Default constructor. Appends expected file name to a file path
	 * 
	 * @param path The directory in which the image files are.
	 */
	public ImageSequence (String path) {
		filePath = path;
	}
	
	/**
	 * Fancy constructor.
	 * 
	 * @param dir The directory to search for input files (only takes .jpeg, this can be changed)
	 */
	public ImageSequence (File dir) {
		fancy = true;
		files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".jpeg");
		    }
		});
	}
	
	@Override
	public ByteBuffer getImage() {
		try {
			//Need to decode file format, then get BGR values as byte[]
			byte[] imgBytes;
			if (fancy) {
				imgBytes = ((DataBufferByte) (ImageIO.read(files[seek++]).getRaster().getDataBuffer())).getData();
			} else {
				imgBytes = ((DataBufferByte) (ImageIO.read(new File(filePath + String.format("%07d", seek++) + ".jpg")).getRaster().getDataBuffer())).getData();
			}
			return ByteBuffer.wrap(imgBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//If the image fails to read or cast
		return null;
	}

	@Override
	public boolean isReady() {
		/*
		if (System.currentTimeMillis() - timeReady >= (1000 / framesPerSecond)) {
			return new File(filePath + String.format("%06d", seek++) + ".jpeg").exists();
		}
		return false;*/
		return true;
	}
	
}