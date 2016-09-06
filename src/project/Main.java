package project;

import java.util.LinkedList;
import static project.Config.*;
import lejos.hardware.Sound;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import static project.Direction.*;

public class Main {

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		Robot robot = new Robot(US_PORT, IR_PORT_VORNE, IR_PORT_HINTEN, MINI_MOTOR_PORT, LINKER_MOTOR_PORT, RECHTER_MOTOR_PORT, LICHT_PORT);
		LinkedList<Integer> memory = robot.getBrain().start(robot.getMemory());

		Sound.beep();
		robot.findeWand();
		Sound.beep();
		Sound.beep();
		Sound.beep();

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
