package project.motors;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import project.Direction;

import static project.Direction.*;

public class Motor {

	private static final int TOP_SPEED = 740;

	private static final int GRAD_FUER_DREHUNG = 380;

	private EV3LargeRegulatedMotor motorLinks;
	private EV3LargeRegulatedMotor motorRechts;

	public Motor(Port linkerMotorPort, Port rechterMotorPort) {
		this.motorLinks = new EV3LargeRegulatedMotor(linkerMotorPort);
		this.motorRechts = new EV3LargeRegulatedMotor(rechterMotorPort);
	}

	public void fahreGerade(int rotationen) {

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		int i = 0;
		while (i < rotationen) {
			DriveSmooth();
			i++;
		}

		motorLinks.endSynchronization();

		motorLinks.waitComplete();
		motorRechts.waitComplete();
	}

	private void DriveSmooth() {
		motorLinks.rotate(90, true);
		motorRechts.rotate(90, false);
	}

	private void setGeschwindigkeitSpezifisch(int percent, Direction lr) {
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

	public void drehe(Direction richtung) {
		if (richtung == LEFT) {
			motorLinks.rotate(GRAD_FUER_DREHUNG);
		} else {
			if (richtung == RIGHT) {
				motorRechts.rotate(GRAD_FUER_DREHUNG);
			}
		}
	}
}
