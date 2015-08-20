import imageAqcuisition.imageInputSource.ImageInputSource;
import imageAqcuisition.imageInputSource.ImageSequence;
import imageProcessing.ImageTools.ImageEntry;
import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;

import java.io.File;
import java.nio.ByteBuffer;


public class MRFWritingTest {
	public static void main(String[] args) throws Exception {
		ImageInputSource src = new ImageSequence("//medixsrv/Nematodes/data/N2_nf7/input/");
		MRFFileOutputStream mrfos = new MRFFileOutputStream(new File("testmrf.mrf"));
		int[] pos = new int[]{123,456};
		byte[] img = new byte[IMAGE_WIDTH * IMAGE_HEIGHT * 3];
		for (int i = 0; i < img.length; i++)
			img[i] = 127;
		ByteBuffer imgBuff = ByteBuffer.wrap(img);
		
		
		for (int i = 0; i < 25; i++) {
			mrfos.writeImageEntry(new ImageEntry(src.getImage(), System.currentTimeMillis(), pos));
		}
		
		mrfos.finish();
		///////
		MRFFileInputStream mrfis = new MRFFileInputStream(new File("testmrf.mrf"));
		int i = 0;
		while (mrfis.hasNext()) {
			System.out.println(i++);
			ImageEntry entry = mrfis.readImageEntry();
		}
		
		mrfis.finish();
		
	}
}
