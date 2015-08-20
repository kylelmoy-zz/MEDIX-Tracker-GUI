package imageRecording;

import imageAcquisition.ImageProducer;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.RECORDING_FLUSH_INTERVAL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

public class DeprecatedImageRecorder implements Runnable {
	private ImageProducer imageProducer;
	private GZIPOutputStream outputStream;
	private Thread thread;
	private int lastFlush;
	private boolean run = true;
	public DeprecatedImageRecorder (ImageProducer imageProducer, File destination) {
		this.imageProducer = imageProducer;
		try {
			outputStream = new GZIPOutputStream(new FileOutputStream(destination));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
		thread = new Thread(this,"Image Recorder");
	}
	public void start() {
		thread.start();
	}
	public void stop() {
		run = false;
	}
	@Override
	public void run() {
		byte[] buff = new byte[IMAGE_WIDTH * IMAGE_HEIGHT * 3];
		while (run) {
			if (imageProducer.size() < 30) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			ImageEntry entry = imageProducer.get();
			synchronized (entry) {
				ByteBuffer img = entry.img;
				img.rewind();
				img.get(buff);
				byte[] gray = ImageTools.toGrayScale(buff);
				try {
					//outputStream.write(entry.toBytes());
					outputStream.write(gray);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (lastFlush++ > RECORDING_FLUSH_INTERVAL) {
				try {
					outputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				lastFlush = 0;
			}
		}
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
