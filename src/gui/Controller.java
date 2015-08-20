package gui;
import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import imageAcquisition.ImageProducer;
import imageAqcuisition.imageInputSource.ImageInputSource;
import imageAqcuisition.imageInputSource.ImageSequence;
import imageAqcuisition.imageInputSource.SerialCamera;
import imageProcessing.ImageProcessor;
import imageProcessing.ImageTools;
import imageProcessing.ImageTools.ImageEntry;
import imageRecording.ImageRecorder;

import java.io.File;
import java.nio.ByteBuffer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import jssc.SerialPortList;
import motorControl.MotorControl;

public class Controller extends VBox {
	private int dragX, dragY;
	private ImageProducer imageProducer;
	private ImageProcessor imageProcessor;
	private MotorControl motorControl;
	private InputViewFeed inputViewFeed;
	private ImageRecorder imageRecorder;
	public Stage stage;
	private File recordingLocation = null;
	private boolean tracking = false;
	private boolean recording = false;
	
	@FXML private ChoiceBox videoInputDeviceList;
	@FXML private ChoiceBox motorControlDeviceList;
	@FXML private ImageView imageView;
	@FXML private Button connectDevicesButton;
	@FXML private Button trackingButton;
	@FXML private Button recordingButton;
	@FXML private TextField fileLocation;

	public void updateImageView (ByteBuffer b) {
		Image img = ImageTools.toJavaFXImage(b); //Oh goodness...
		imageView.setImage(img);
	}

	@FXML
	protected void browseFileLocation() {
		/*
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Recording save location");
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MRF", "*.mrf"),
                new FileChooser.ExtensionFilter("All Images", "*.*")
            );
		recordingLocation = fileChooser.showSaveDialog(stage);
		fileLocation.setText(recordingLocation.getAbsolutePath());
		*/
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Recording Location");
		chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		recordingLocation = chooser.showDialog(stage);
		fileLocation.setText(recordingLocation.getAbsolutePath());
	}
	@FXML
	protected void connectDevices() {
		try {
			ImageInputSource imageSource = new ImageSequence("//medixsrv/Nematodes/data/N2_nf20/input/");//new SerialCamera();//new ImageSequence("//medixsrv/Nematodes/data/N2_nf7/input/");//
	    	imageProducer = new ImageProducer(imageSource);
			motorControl = new MotorControl((String)motorControlDeviceList.getSelectionModel().getSelectedItem());
			imageProducer.start();
			inputViewFeed = new InputViewFeed(imageProducer,this);
			inputViewFeed.start();
		} catch (Exception e) {
			alert("Error", "Device not found", "There was an error trying to connect to the selected device.");
		} 
	}

	@FXML
	protected void tracking() {
		if (tracking) {
			motorControl.stop();
			trackingButton.setText("Start Tracking");
		} else {
			if (imageProducer == null) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("No devices connected");
				alert.setContentText("Please connect a camera and motor control device before continuing.");
				alert.showAndWait();
				return;
			}
			if (imageProcessor == null) {
				imageProcessor = new ImageProcessor(imageProducer);
				motorControl.attach(imageProcessor);
				imageProcessor.start();
			}
			imageProcessor.run = true;
			motorControl.start();
			inputViewFeed.attach(imageProcessor);
			trackingButton.setText("Stop Tracking");
		}
		tracking = !tracking;
	}
	
	@FXML
	protected void recording() {
		if (recording) {
			imageRecorder.stop();
			recordingButton.setText("Start Recording");
		} else {
			if (imageProducer == null) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("No devices connected");
				alert.setContentText("Please connect a camera and motor control device before continuing.");
				alert.showAndWait();
				return;
			}
			if (recordingLocation == null) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("No recording location specified");
				alert.setContentText("Please set a save location for the recording file, under the 'Options' tab.");
				alert.showAndWait();
				return;
			}
			imageRecorder = new ImageRecorder(imageProducer, recordingLocation);
			imageRecorder.start();
			fileLocation.setText("");
			recordingLocation = null;
			recordingButton.setText("Stop Recording");
		}
		recording = !recording;
	}
	
	@FXML
	protected void dragPressed(MouseEvent event) {
		dragX = (int) event.getX();
		dragY = (int) event.getY();
	}

	@FXML
	protected void dragReleased(MouseEvent event) {
		if (motorControl == null) return;
		int deltaX = (int) event.getX() - dragX;
		int deltaY = (int) event.getY() - dragY;
		//System.out.println(deltaX + "\t" + deltaY);
		
		//Oh goodness...
		(new Thread() {
			public void run() {
				motorControl.move(deltaX, deltaY);
			}
		}).start();
	}
	@FXML
	protected void up() {
		if (motorControl == null) return;
		(new Thread() { public void run() {
				motorControl.move(0, -IMAGE_HEIGHT);
		}}).start();
	}
	@FXML
	protected void down() {
		if (motorControl == null) return;
		(new Thread() { public void run() {
				motorControl.move(0, IMAGE_HEIGHT);
		}}).start();
	}

	@FXML
	protected void left() {
		if (motorControl == null) return;
		(new Thread() { public void run() {
				motorControl.move(-IMAGE_WIDTH, 0);
		}}).start();
	}

	@FXML
	protected void right() {
		if (motorControl == null) return;
		(new Thread() { public void run() {
				motorControl.move(IMAGE_WIDTH, 0);
		}}).start();
	}


	@FXML
	protected void refreshVideoInputDevices() {
    	Platform.runLater(new Runnable() {
            @Override public void run() {
				String[] cams = SerialCamera.getCameras();
				if (cams.length == 0)
					videoInputDeviceList.getItems().add("No Devices Detected");
				for (String s : cams) {
					videoInputDeviceList.getItems().add(s);
				}
				
				String[] ports = SerialPortList.getPortNames();
				if (ports.length == 0)
					motorControlDeviceList.getItems().add("No Devices Detected");
		        for(String s : ports){
		        	motorControlDeviceList.getItems().add(s);
		        }
				motorControlDeviceList.getSelectionModel().selectFirst();
				videoInputDeviceList.getSelectionModel().selectFirst();
			}
    	});
	}

	
	
	public void stop() {
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	if (imageRecorder != null) {
            		imageRecorder.stop();
            		imageRecorder = null;
    				recordingButton.setText("Start Recording");
            	}
    			motorControl.stop();
    			trackingButton.setText("Start Tracking");
            }
        });
	}
	
    public static class InputViewFeed implements Runnable {
    	ImageProducer src;
    	ImageProcessor prc;
        Controller dest;
        Thread thread;
    	public InputViewFeed(ImageProducer source, Controller destination) {
    		src = source;
    		dest = destination;
    		thread = new Thread(this, "InputViewFeed");
    	}
    	public void attach(ImageProcessor processor) {
    		prc = processor;
    	}
    	public void start() {
    		thread.start();
    	}
		@Override
		public void run() {
			while (dto.Properties.run) {
				ImageEntry entry = src.peek();
				if (entry == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				ByteBuffer clone;
				synchronized(entry) {
					ByteBuffer img = entry.img;
					clone = ByteBuffer.allocate(img.capacity());
					img.rewind();
					clone.put(img);
					img.rewind();
				}
				clone.flip();
				if (prc != null) dest.updateImageView(prc.overlayImage(clone));
				else dest.updateImageView(clone);
			}
		}
    }
    
    public static void alert(String title, String header, String text) {
    	Platform.runLater(new Runnable() {
            @Override public void run() {
            	Alert alert = new Alert(AlertType.INFORMATION);
        		alert.setTitle(title);
        		alert.setHeaderText(header);
        		alert.setContentText(text);
        		alert.showAndWait();
            }
        });
    }
}