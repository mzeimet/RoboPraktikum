package project.motors;

import static project.Direction.FORWARD;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import project.Direction;

/**
 * Dreht entweder um 90 Grad nach Links oder Rechts, jedoch nur abwechselnd
 */
public class MiniMotor {
	

	private EV3MediumRegulatedMotor motor;

	private Direction ausrichtung = FORWARD;

	private static final int MAX_GRADZAHL = 100;

	private int gradzahl;

	public MiniMotor(Port port) {
		this.motor = new EV3MediumRegulatedMotor(port);
	}

	public void drehe(Direction neueRichtung) {
		motor.rotate(berechneGradZuDrehen(ausrichtung, neueRichtung));
		this.ausrichtung = neueRichtung;
	}

	/**
	 * Dreht den Motor um die angegebene gradzahl relativ zur Mitte. negativ für
	 * nach links, positiv für rechts. kleiner
	 * 
	 * @param grad
	 */
	public void drehe(int grad) {
		grad *= -1;
		if (Math.abs(grad) > MAX_GRADZAHL) {
			throw new IllegalArgumentException();
		}
		motor.rotate(grad - gradzahl);
		gradzahl = grad;
	}

	/**
	 * dreht den motor wieder in die mitte zurück
	 */
	public void dreheZurueck() {
		motor.rotate(-gradzahl);
		gradzahl = 0;
	}

	public int berechneGradZuDrehen(Direction aktuelleRichtung, Direction neueRichtung) {
		int differenz = Math.abs(neueRichtung.ordinal() - aktuelleRichtung.ordinal());
		if (differenz > 2)
			System.exit(5);
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

	public static int getMaxGradzahl() {
		return MAX_GRADZAHL;
	}

}
