package imageAqcuisition.imageInputSource;

import java.nio.ByteBuffer;

/**
 * An image input object, allows you to get images.
 * 
 * @author Kyle Moy
 *
 */
public interface ImageInputSource {
	public ByteBuffer getImage();
	public boolean isReady();
}
