package imageAcquisition;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.util.OsUtils;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Camera {

    private static Webcam webcam;
    private static String CURRENT_DIRECTORY = "output/";
    public static int recordLength = 0; //ms

    //Creating shared object
    static BlockingQueue<ByteBuffer> sharedQueue = new LinkedBlockingQueue();

    //Creating Producer and Consumer Thread
    static Thread recorderThread = new Thread(new CameraImageRecorder(sharedQueue));
    static Thread writerThread = new Thread(new CameraByteImageWriter(sharedQueue));
    //static Thread imgProcessThread = new Thread(new ImageProcessing(sharedQueue));
    //static Thread motorThread = new Thread(new MotorController());

    public Camera(int ms) {
        this.recordLength = ms;
        //deleteFolderFiles(CURRENT_DIRECTORY);
        setDriver();
        setWebcam();
        startCamera();

        // Start image writer thread
        writerThread.start();
    }

    private void setDriver() {
        System.out.println("[setDriver] OS CHECK: " + OsUtils.getOS());
        String driverName = "";
        switch (OsUtils.getOS()) {
            case WIN:
                driverName = "DEFAULT";
                Webcam.setDriver(new WebcamDefaultDriver());
                break;
            case OSX:
                driverName = "DEFAULT";
                Webcam.setDriver(new WebcamDefaultDriver());
                break;
            case NIX:
                // Currently using Default Driver
                // V4L4J can be modified here
                driverName = "DEFAULT";
                Webcam.setDriver(new WebcamDefaultDriver());
                break;
            default:
                throw new RuntimeException("Capture device not supported on " + OsUtils.getOS());
        }
        System.out.println("DRIVER SELECTED: " + driverName + ", FOR OS: " + OsUtils.getOS());
    }

    /*
    private void deleteFolderFiles(String folderDirectory) {
        File workingDirectory = new File(folderDirectory);
        int numOfFiles = 0;
        if (workingDirectory.exists()) {
            File[] files = workingDirectory.listFiles();
            for (File file : files) {
                file.delete();
                numOfFiles++;
            }
        }
        System.out.println("STATUS: Deleted " + numOfFiles
                + " file(s) in folder \"" + workingDirectory.getAbsoluteFile()
                + "\"");
    }
    */

    private void setWebcam() {
        List<Webcam> webcamList = Webcam.getWebcams();
        if (webcamList.size() == 0) {
            System.out.print("ERROR: NO Webcam detected.");
        } else if (webcamList.size() > 1) {
            Scanner in = new Scanner(System.in);
            System.out.println("WARNING: Multiple webcams detected. Select one of the following [int]:");
            int camCount = 0;
            for (Webcam cam : webcamList) {
                System.out.println("[" + camCount + "]: " + cam);
                camCount++;
            }
            System.out.print("> ");
            Integer selectedCam = in.nextInt();
            webcam = webcamList.get(selectedCam); // user selected cam
        } else {
            webcam = Webcam.getDefault(); // grabs default, first cam from list
        }
        System.out.println("CAMERA SELECTED: " + webcam.getName());
    }

    private void startCamera() {
        // Hardcoded Resolution
        webcam.setCustomViewSizes(new Dimension[]{new Dimension(1280, 960)});
        webcam.setViewSize(new Dimension(1280, 960)); // set custom size

        webcam.open(false); // open camera state, in regular blocking mode

        System.out.println("RESOLUTION: " + webcam.getViewSize());

        // Sleep to prevent initial green/black image
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void stopCamera() {
        webcam.close();
    }

    public static boolean isCameraClosed() {
        return !webcam.isOpen();
    }

    public static Dimension getResolution() {
        return webcam.getViewSize();
    }

    public static void capture() {
        //Starting producer thread
        recorderThread.start();
    }

    public static Webcam getCamera() {
        return webcam;
    }

    public static ByteBuffer getImageBytes() {
        ByteBuffer imgBytes = webcam.getImageBytes();
        return imgBytes;
    }

    public static boolean isImageNew() {
        Boolean b = webcam.isImageNew();
        return b;
    }

    public void close() {
        webcam.close();
    }
}




