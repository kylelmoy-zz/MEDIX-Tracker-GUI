package motorControl;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Main {
	public static void main(String[] args) throws Exception {
		String[] names = SerialPortList.getPortNames();
		SerialPort serialPort = new SerialPort("COM4");
	    try {
	        System.out.println("Port opened: " + serialPort.openPort());
	        System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));
	        //System.out.println("Write to port: " + serialPort.writeBytes(("V\r").getBytes()));
	        System.out.println("Write to port: " + serialPort.writeBytes(("EM,5,5\r").getBytes()));
	        int speed = 500;
	        //Reset to 
    		serialPort.writeBytes(("SM," + speed + ",11,11\r").getBytes());
        	Thread.sleep(speed);
        	
    		//serialPort.writeBytes(("SM," + speed + ",5000,5000\r").getBytes());
        	//Thread.sleep(speed);
        	/*
    		serialPort.writeBytes(("SM," + speed + ",-5000,-5000\r").getBytes());
        	Thread.sleep(speed);
    		serialPort.writeBytes(("SM," + speed + ",-5000,5000\r").getBytes());
        	Thread.sleep(speed);
    		serialPort.writeBytes(("SM," + speed + ",5000,5000\r").getBytes());
        	Thread.sleep(speed);
    		serialPort.writeBytes(("SM," + speed + ",5000,-5000\r").getBytes());
        	Thread.sleep(speed);
        	*/
	        /*
	        int r = 3000;
	        int speed = 200;
	        int x = r;
	        int y = 0;
	        int tx = r;
	        int ty = 0;
	        for (double i = 0; i <= Math.PI * 2; i+= Math.PI / 64) {
	        	int mx = 0;
	        	int my = 0;
	        	if (x != tx) mx = tx - x;
	        	if (y != ty) my = ty - y;
	        	if (mx != 0 || my != 0) {
	        		double distance = Math.sqrt(Math.pow(mx, 2) + Math.pow(my,2));
	        		double ratio = distance / 1000; //millimeters
	        		int delay = (int)(ratio * speed);
	        		serialPort.writeBytes(("SM," + delay + "," + mx + "," + my + "\r").getBytes());
	        		System.out.println("SM," + delay + "," + mx + "," + my);
	        		x = tx;
	        		y = ty;
		        	Thread.sleep(speed);
	        	}
	        	tx = (int)(r * Math.cos(i));
	        	ty = (int)(r * Math.sin(i));
	        	//System.out.println(tx + ", " + ty);
	        }
	        */
	        System.out.println("Write to port: " + serialPort.writeBytes(("EM,0,0\r").getBytes()));
	        //Thread.sleep(1000);
	        //System.out.println("Write to port: " + serialPort.writeBytes(("SM,1000,200,200\r").getBytes()));
	        //Thread.sleep(1000);
	        System.out.println("Port closed: " + serialPort.closePort());
	    }
	    catch (SerialPortException ex){
	        System.out.println(ex);
	    }
	}
}
