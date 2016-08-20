package project;



import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import project.motors.Motor;

public class Main {

	public static final String LICHT_PORT_LINKS = "S1";
	public static final String LICHT_PORT_RECHTS = "S4";

	public static final String IR_PORT = "S3";
	
	public static final String US_PORT = "S2";

	public static final Port LINKER_MOTOR_PORT = MotorPort.B;
	public static final Port RECHTER_MOTOR_PORT = MotorPort.C;
	public static final Port MINI_MOTOR_PORT = MotorPort.A;

	public static final int SCHWELLWERT_STOP = 80;
	private static final int START_SPEED = 30;

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		Robot robot = new Robot(US_PORT, IR_PORT, MINI_MOTOR_PORT, LINKER_MOTOR_PORT, RECHTER_MOTOR_PORT);
		//robot.findeWand();
		//robot.linksdrehungAufDerStelle();
		//robot.rechtsdrehungAufDerStelle();
		//robot.rechtsDrehen();
	}

	private boolean kleinerSchwellwert(int percentLeft, int percentRight) {
		return percentLeft < SCHWELLWERT_STOP && percentRight < SCHWELLWERT_STOP;
	}

	// private void searchLight(Robot robot) {
	// robot.setGeneralSpeed(START_SPEED);
	// robot.runForward();
	//
	// int lightPercentLeft = 0;
	// int lightPercentRight = 0;
	//
	// while (kleinerSchwellwert(lightPercentLeft, lightPercentRight)) {
	// lightPercentLeft = robot.getLigthInPercent(LEFT);
	// lightPercentRight = robot.getLigthInPercent(RIGHT);
	// accelerateTowardsLight(lightPercentLeft, lightPercentRight, robot);
	// Delay.msDelay(250);
	// }
	// robot.standStill();
	// }
	//
	// private void accelerateTowardsLight(int lightPercentLeft, int
	// lightPercentRight, Robot robot) {
	// int diff = lightPercentLeft - lightPercentRight;
	// if(diff <0){// rechts groesser
	// lightPercentLeft+=10 *diff;
	// }else{//links groesser
	// lightPercentRight+=10 *diff;
	// }
	// robot.setSpeedInPercent(lightPercentRight, LEFT);
	// robot.setSpeedInPercent(lightPercentLeft, RIGHT);
	// System.out.println("Motor R:"+lightPercentLeft + "\n" +"Motor L:"+
	// lightPercentRight);
	// }
}
