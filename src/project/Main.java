package project;

import static project.Config.IR_PORT_HINTEN;
import static project.Config.IR_PORT_VORNE;
import static project.Config.LICHT_PORT;
import static project.Config.LINKER_MOTOR_PORT;
import static project.Config.MINI_MOTOR_PORT;
import static project.Config.RECHTER_MOTOR_PORT;
import static project.Config.SCHWELLWERT_STOP;
import static project.Config.US_PORT;

import java.util.LinkedList;

import lejos.hardware.Sound;

public class Main {

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		Robot robot = new Robot(US_PORT, IR_PORT_VORNE, IR_PORT_HINTEN, MINI_MOTOR_PORT, LINKER_MOTOR_PORT,
				RECHTER_MOTOR_PORT, LICHT_PORT);

		Sound.beep();
		robot.findeWand();
		LinkedList<Integer> memory = robot.getBrain().start(robot.getMemory());
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
