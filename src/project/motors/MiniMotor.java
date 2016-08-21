package project.motors;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import project.Direction;

import static project.Direction.*;

/**
 * Dreht entweder um 180 Grad nach Links oder Rechts, jedoch nur abwechselnd
 */
public class MiniMotor {

	private EV3MediumRegulatedMotor motor;

	private Direction ausrichtung = FORWARD;

	public MiniMotor(Port port) {
		this.motor = new EV3MediumRegulatedMotor(port);
	}

	public void drehe(Direction neueRichtung) {
		motor.rotate(berechneGradZuDrehen(ausrichtung, neueRichtung));
		this.ausrichtung = neueRichtung;
	}

	public int berechneGradZuDrehen(Direction aktuelleRichtung, Direction neueRichtung) {
		int differenz = Math.abs(neueRichtung.ordinal() - aktuelleRichtung.ordinal());
		if(differenz > 2) System.exit(5);
		if (aktuelleRichtung.ordinal() > neueRichtung.ordinal()) {
			return 90 * differenz;
		} else {
			return -90 * differenz;
		}
	}

	public Direction getAusrichtung() {
		return ausrichtung;
	}

	public void setAusrichtung(Direction ausrichtung) {
		this.ausrichtung = ausrichtung;
	}

}
