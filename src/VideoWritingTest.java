import java.io.File;

import org.jcodec.api.SequenceEncoder;

import imageAqcuisition.imageInputSource.ImageInputSource;
import imageAqcuisition.imageInputSource.ImageSequence;
import imageProcessing.ImageTools;

public class VideoWritingTest {
	public static void main (String[] args) throws Exception {
		ImageInputSource src = new ImageSequence("//medixsrv/Nematodes/data/N2_nf7/input/");
		File out = new File ("test.mp4");
		SequenceEncoder encoder = new SequenceEncoder(out);
		System.out.println("Start Encoding");
		for (int i = 1; i < 1024; i++) {
			System.out.println(i);
			encoder.encodeImage(ImageTools.toBufferedImage(src.getImage()));
		}
		encoder.finish();
		System.out.println("Done!");
	}
}
