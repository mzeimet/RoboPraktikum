package project.motors;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;

/**
 * Dreht entweder um 180 Grad nach Links oder Rechts, jedoch nur abwechselnd
 */
public class MiniMotor {

	private EV3MediumRegulatedMotor motor;

	private boolean richtungLinks = true;

	public MiniMotor(Port port) {
		this.motor = new EV3MediumRegulatedMotor(port);
	}

	public void invertiere() {
		if (richtungLinks) {
			motor.rotate(-180);
			richtungLinks = false;
		} else {
			motor.rotate(180);
			richtungLinks = true;
		}

	}

	public boolean isRichtungLinks() {
		return richtungLinks;
	}

	public void dreheLinks() {
		if (richtungLinks) {
			throw new IllegalArgumentException("Steht schon links!");
		} else {
			motor.rotate(-180);
			richtungLinks = false;
		}
	}

	public void dreheRechts() {
		if (!richtungLinks) {
			throw new IllegalArgumentException("Steht schon links!");
		} else {
			motor.rotate(180);
			richtungLinks = true;
		}
	}
}
