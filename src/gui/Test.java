package gui;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;

import java.awt.Dimension;
import java.io.File;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class Test {
	public static void main(String[] args) throws Exception {
		Webcam camera = Webcam.getDefault(); 
    	camera.setCustomViewSizes(new Dimension[]{new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT)});
    	camera.setViewSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
    	camera.open(false);
    	ImageIO.write(camera.getImage(),"png",new File("test.png"));
	}
}
