

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.RECORDING_FLUSH_INTERVAL;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

public class MRFFileOutputStream {
	GZIPOutputStream os;
	public MRFFileOutputStream (File file) throws FileNotFoundException, IOException {
		os = new GZIPOutputStream(new FileOutputStream(file));
	}
	
	public void writeImageEntry (ImageEntry entry) throws IOException {
		byte[] buff = new byte[IMAGE_WIDTH * IMAGE_HEIGHT * 3];
		byte[] metaData;
		byte[] imageData;
		synchronized (entry) {
			ByteBuffer img = entry.img;
			img.rewind();
			img.get(buff);
			//metaData = entry.toBytes();
		}
		imageData = ImageTools.toGrayScale(buff);
		//System.out.println(metaData.length + imageData.length);
		//os.write(metaData);
		os.write(imageData);
		os.flush();
	}
	
	public void finish() throws IOException {
		os.finish();
		os.close();
	}
}
