package project.motors;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import project.Direction;

public class Motor {

	private static final int TOP_SPEED = 740;

	private EV3LargeRegulatedMotor motorLinks;
	private EV3LargeRegulatedMotor motorRechts;

	public Motor(Port linkerMotorPort, Port rechterMotorPort) {
		this.motorLinks = new EV3LargeRegulatedMotor(linkerMotorPort);
		this.motorRechts = new EV3LargeRegulatedMotor(rechterMotorPort);
	}

	public void fahreGerade() {

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		int i = 0;
		while (i < 30) {
			DriveSmooth(i);
		}

		motorLinks.endSynchronization();

		motorLinks.waitComplete();
		motorRechts.waitComplete();

	}

	private void DriveSmooth(int i) {

		motorLinks.rotate(i, true);
		motorRechts.rotate(i, true);

	}

	private void setGeschwindigkeitSpezifisch(int percent, Direction lr) {
		percent = validateOrCorrectPercent(percent);
		if (lr.equals(Direction.LEFT)) {
			motorLinks.setSpeed(percent * TOP_SPEED / 100);
			motorLinks.forward();
		} else {
			motorRechts.setSpeed(percent * TOP_SPEED / 100);
			motorRechts.forward();
		}
	}

	public void setGeschwindigkeit(int speedInPercent) {
		setGeschwindigkeitSpezifisch(speedInPercent, Direction.LEFT);
		setGeschwindigkeitSpezifisch(speedInPercent, Direction.RIGHT);
	}

	private int validateOrCorrectPercent(int percent) {
		if (percent > 100) {
			return 100;
		}
		if (percent < 0) {
			return 1;
		}
		return percent;
	}

	public void turnLeft() {
		motorLinks.rotate(380);
	}

	public void turnRight() {
		motorRechts.rotate(380);
	}

}
