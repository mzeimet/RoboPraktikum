package project.motors;

import static project.Direction.LEFT;
import static project.Direction.RIGHT;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import project.Direction;

public class Motor {

	private static final int TOP_SPEED = 740;
	private static final double RADABSTAND = 12.2;
	private static final double RADDURCHMESSER = 6;
	private static final double BODENFAKTOR = 1; // je schlechter der Boden,
													// desto hoeher der Faktor

	private static final int GRAD_FUER_DREHUNG = 380;

	private EV3LargeRegulatedMotor motorLinks;
	private EV3LargeRegulatedMotor motorRechts;

	public Motor(Port linkerMotorPort, Port rechterMotorPort) {
		this.motorLinks = new EV3LargeRegulatedMotor(linkerMotorPort);
		this.motorRechts = new EV3LargeRegulatedMotor(rechterMotorPort);
	}

	public void fahreGerade() {

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		DriveSmooth();

		motorLinks.endSynchronization();

		motorLinks.waitComplete();
		motorRechts.waitComplete();
	}

	public void stop() {
		motorLinks.flt();
	}

	private void DriveSmooth() {
		int geschwindigkeit = 0;
		motorLinks.setSpeed(geschwindigkeit);
		motorRechts.setSpeed(geschwindigkeit);

		motorLinks.forward();
		motorRechts.forward();

		while (geschwindigkeit < END_WERT) {
			geschwindigkeit++;
			motorLinks.setSpeed(geschwindigkeit);
			motorRechts.setSpeed(geschwindigkeit);
		}
	}

	public void setGeschwindigkeitSpezifisch(int percent, Direction lr) {
		percent = validateOrCorrectPercent(percent);
		if (lr.equals(LEFT)) {
			motorLinks.setSpeed(percent * TOP_SPEED / 100);
		} else {
			motorRechts.setSpeed(percent * TOP_SPEED / 100);
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
			drehenAufDerStelle(-90);
		} else {
			if (richtung == RIGHT) {
				drehenAufDerStelle(90);
			}
		}
	}

	/**
	 * Berechnet wie oft sich das Rad drehen muss, damit der Roboter sich einmal
	 * um 360° dreht.
	 * 
	 * @return umdrehungen
	 */
	public double berechneUmdrehungenProRunde() {
		double radumfang = RADDURCHMESSER * Math.PI;
		double umdrehungen = (Math.PI * RADABSTAND) / radumfang;
		return umdrehungen;
	}

	/**
	 * Dreht sich um die eingegebene Gradzahl auf der Stelle. Negative Gradzahl
	 * => Linksdrehung, Positive Gradzahl => Rechtsdrehung
	 * 
	 * @param grad,
	 *            gibt an um wie viel Grad sich der Roboter drehen soll
	 */
	public void drehenAufDerStelle(int grad) {

		double linksVorher = motorLinks.getTachoCount();
		double rechtsVorher = motorRechts.getTachoCount();

		double linksGrad = 0;
		double rechtsGrad = 0;
		if (grad == 0)
			return;

		rechtsGrad = berechneUmdrehungenProRunde() * grad * BODENFAKTOR;
		linksGrad = -1 * rechtsGrad;

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		motorLinks.setSpeed(70);
		motorLinks.rotate((int) linksGrad, true);

		motorRechts.setSpeed(70);
		motorRechts.rotate((int) rechtsGrad, false);

		motorLinks.endSynchronization();
		motorLinks.waitComplete();
		motorRechts.waitComplete();

		double linksNachher = motorLinks.getTachoCount();
		double rechtsNachher = motorRechts.getTachoCount();
		double differenzLinks = linksVorher - linksNachher;
		double differenzRechts = rechtsVorher - rechtsNachher;
		System.out.println("Links: " + differenzLinks + ", Rechts: " + differenzRechts);
	}

	public void fahreGerade(double f) {
		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		motorLinks.rotate((int) (f * 360));
		motorRechts.rotate((int) (f * 360));

		motorLinks.endSynchronization();

		motorLinks.waitComplete();
		motorRechts.waitComplete();

	}

	public int getTachoCount() {
		return motorLinks.getTachoCount();
	}
}
