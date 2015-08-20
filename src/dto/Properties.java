package dto;

public class Properties {
	
	public static volatile boolean run = true;
	
	public static int IMAGE_WIDTH = 640;
	public static int IMAGE_HEIGHT = 480;
	
	public static int IMAGE_BUFFER_SIZE = 120;
	
	public static int RECORDING_FLUSH_INTERVAL = 30; //In frames

	public static int SEGMENTATION_WINDOW_SIZE = 25;
	public static int SEGMENTATION_COMPONENT_MIN_SIZE = 5;
	public static double SEGMENTATION_THRESHOLD = 0.80; //Magic number. Don't touch.
	public static int SEGMENTATION_DELAY = 333;
	public static int SEGMENTATION_FAILURE_THRESHOLD = 600000; //Time until we give up trying
	
	
	/**
	 * MOTOR_STEP_MODE controls what step mode the stepper motors will be enabled in.
	 * 1 will enable both axis in 1/16th step mode (default on boot)
	 * 2 will enable both axis in 1/8th step mode
	 * 3 will enable both axis in 1/4 step mode
	 * 4 will enable both axis in 1/2 step mode
	 * 5 will enable both axis in full step mode
	 */
	public static int MOTOR_STEP_MODE = 1;
	public static int[] MOTOR_STEP_MODE_MULTIPLIER_VALUES = {16,8,4,2,1};
	public static int MOTOR_STEP_MULTIPLIER = MOTOR_STEP_MODE_MULTIPLIER_VALUES[MOTOR_STEP_MODE - 1];
	public static double MOTOR_PX_PER_STEP = 13.913; 
	public static int MOTOR_MOVE_DELAY = 500;
	public static int MOTOR_TICK_RATE = 10;

	public static double MOVE_DECISION_BOUNDARY_RATIO = 0.15;
	public static int MOVE_DECISION_BOUNDARY_PX = (int)(IMAGE_WIDTH * MOVE_DECISION_BOUNDARY_RATIO);
	public static double MOVE_DECISION_CONFIDENCE_DISTANCE = 25;
	public static int MOVE_DECISION_POST_DELAY = 1000;

}
