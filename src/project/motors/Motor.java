package project.motors;

import static project.LeftRight.LEFT;
import static project.LeftRight.RIGHT;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import project.LeftRight;

public class Motor {

	private static final int TOP_SPEED = 740;

	private EV3LargeRegulatedMotor motorLinks;
	private EV3LargeRegulatedMotor motorRechts;

	public Motor(Port linkerMotorPort, Port rechterMotorPort) {
		this.motorLinks = new EV3LargeRegulatedMotor(linkerMotorPort);
		this.motorRechts = new EV3LargeRegulatedMotor(rechterMotorPort);
	}

	public void fahreGerade() {
		motorLinks.forward();
		motorRechts.forward();
	}

	private void setGeschwindigkeitSpezifisch(int percent, LeftRight lr) {
		percent = validateOrCorrectPercent(percent);
		if (lr.equals(LEFT)) {
			motorLinks.setSpeed(percent * TOP_SPEED / 100);
			motorLinks.forward();
		} else {
			motorRechts.setSpeed(percent * TOP_SPEED / 100);
			motorRechts.forward();
		}
	}

	public void setGeschwindigkeit(int speedInPercent) {
		setGeschwindigkeitSpezifisch(speedInPercent, LEFT);
		setGeschwindigkeitSpezifisch(speedInPercent, RIGHT);
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

}
