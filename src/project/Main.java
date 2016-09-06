package project;

import java.util.LinkedList;

import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

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
		Robot robot = new Robot(LINKER_MOTOR_PORT, RECHTER_MOTOR_PORT);

		LinkedList<Integer> memory = robot.getBrain().start(robot.getMemory());
		robot.doWhatLemmingsDo(memory);
	}

}
