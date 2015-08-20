package imageAcquisition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.zip.GZIPOutputStream;

class CameraByteImageWriter implements Runnable {

    public static boolean isDone = false;

    private final BlockingQueue<ByteBuffer> sharedQueue;
    ByteBuffer imgBytes = null;

    public CameraByteImageWriter(BlockingQueue<ByteBuffer> sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {
        // FileOutputStream writes directly to disk, instead of ByteArrayOutputStream storing entire contents in memory

        // TODO: Determine file naming convention
        try {
            GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File("worms.zip")));

            int i = 0;
            while (true) {

                try {
                    imgBytes = sharedQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] bytes = new byte[Camera.getResolution().width /*width*/ * Camera.getResolution().height /*height*/ * 1 /*bits per pixel*/];
                imgBytes.get(bytes);

                System.out.println("<Consumer>: File" + i + ", " + bytes);

                zip.write(bytes);

                i++;

                if (isDone && sharedQueue.isEmpty()) {
                    zip.flush();
                    zip.close();
                    return;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
