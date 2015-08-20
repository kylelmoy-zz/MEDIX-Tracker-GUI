package imageAcquisition;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/*
Image Acquisition – (Thread 1)

•	V4L4J Driver (MODIFIED: Using Modified .so file with grey format instead of rgb24, for the builtin driver. Program works with any driver that returns image as bytes.)
•	Sarxos Webcam Interface (MODIFIED: Capturing in GREY format, sarxos library does not support grey but it does not matter. Can construct image from bytearray)
•	getImage()  (MODIFIED: grabbing as bytebuffer -> byte[])
•	Image Buffer
 */

class CameraImageRecorder implements Runnable {

    private final BlockingQueue<ByteBuffer> sharedQueue;

    public CameraImageRecorder(BlockingQueue sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {
        int count = 0;
        long startTime = System.currentTimeMillis();

        // TODO: Modify to start/stop based on worm location
        while ((System.currentTimeMillis() - startTime) < (Camera.recordLength)) { // capture
            ByteBuffer image = Camera.getImageBytes();
            sharedQueue.add(image);
            System.out.println("<Producer>: File" + count);
            count++;
        }

        Camera.stopCamera();
        // Stops loop and finishes writing to zip file; getter/setter later
        CameraByteImageWriter.isDone = true;
    }

}