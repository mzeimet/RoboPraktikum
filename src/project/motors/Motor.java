package project.motors;

import static project.Config.BODENFAKTOR;
import static project.Config.RADABSTAND;
import static project.Config.RADDURCHMESSER;
import static project.Config.START_SPEED;
import static project.Config.TOP_SPEED;
import static project.Direction.LEFT;
import static project.Direction.RIGHT;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import project.Direction;

public class Motor {

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

	}

	public void stop() {

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		motorLinks.flt();
		motorRechts.flt();

		motorLinks.endSynchronization();
	}

	private void DriveSmooth() {
		int geschwindigkeit = 0;
		setGeschwindigkeitSpezifisch(geschwindigkeit, Direction.RIGHT);
		setGeschwindigkeitSpezifisch(geschwindigkeit, Direction.LEFT);

		while (geschwindigkeit < START_SPEED) {

			geschwindigkeit++;
			setGeschwindigkeitSpezifisch(geschwindigkeit, Direction.RIGHT);
			setGeschwindigkeitSpezifisch(geschwindigkeit, Direction.LEFT);
			motorLinks.forward();
			motorRechts.forward();
		}
	}

	public void setGeschwindigkeitSpezifisch(float f, Direction lr) {
		f = validateOrCorrectPercent(f);
		if (lr.equals(LEFT)) {
			motorLinks.setSpeed(f * TOP_SPEED / 100);
		} else {
			motorRechts.setSpeed(f * TOP_SPEED / 100);
		}
	}

	public void setGeschwindigkeit(int speedInPercent) {
		setGeschwindigkeitSpezifisch(speedInPercent, LEFT);
		setGeschwindigkeitSpezifisch(speedInPercent, RIGHT);
	}

	private float validateOrCorrectPercent(float f) {
		if (f > 100) {
			return 100;
		}
		if (f < 0) {
			return 1;
		}
		return f;
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

	public void driveTachoCount(Integer i) {

		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		motorLinks.setSpeed(30 * 740 / 100);
		motorRechts.setSpeed(30 * 740 / 100);

		motorLinks.rotate(i);

		motorRechts.rotate(i);

		motorLinks.flt();
		motorRechts.flt();
		motorLinks.endSynchronization();

		motorLinks.waitComplete();
		motorRechts.waitComplete();

	}

	public int getTachoCount() {
		return motorLinks.getTachoCount();
	}

	public void forward() {
		motorLinks.forward();
		motorRechts.forward();

	}
}
