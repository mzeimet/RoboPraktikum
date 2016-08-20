package project.motors;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;
import project.Direction;


public class Motor {

	private static final int TOP_SPEED = 740;
	private static final double RADABSTAND = 12.2;
	private static final double RADUMFANG = 17.6;
	private static final double DREIHUNDERTSECHZIG_GRAD = 360;

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
	
	/**
	 * Berechnet wie oft sich das Rad drehen muss, damit der Roboter sich einmal um 360° dreht.
	 * umdrehungen, sooft muss sich das Rad drehen, damit der Roboter sich einmal um 360° dreht.
	 * 
	 * @return umdrehungenInGrad, 
	 */
	public double berechneUmdrehungenProRunde() {
			double umdrehungen = (Math.PI * RADABSTAND) / RADUMFANG;
			double umdrehungenInGrad = DREIHUNDERTSECHZIG_GRAD * umdrehungen;
			return umdrehungenInGrad;
	}
	
	/**
	 * Dreht sich um die eingegebene Gradzahl nach links auf der Stelle
	 * 
	 * @param grad, gibt an um wie viel Grad sich der Roboter drehen soll
	 */
	public void linksdrehungAufDerStelle(int grad) {
		double linksGrad = berechneUmdrehungenProRunde() * grad;
		double rechtsGrad = -1 * linksGrad;
		
		motorLinks.synchronizeWith(new RegulatedMotor[] { motorRechts });
		motorLinks.startSynchronization();

		
		motorLinks.setSpeed(70);
		motorLinks.rotate((int) linksGrad,true);
		
		motorRechts.setSpeed(70);
		motorRechts.rotate((int) rechtsGrad, true);
		
		motorLinks.endSynchronization();
		motorLinks.waitComplete();
		motorRechts.waitComplete();		
	}
	
	/**
	 * Dreht sich um die eingegebene Gradzahl nach rechts auf der Stelle
	 * 
	 * @param grad, gibt an um wie viel Grad sich der Roboter drehen soll
	 */
	public void rechtsdrehungAufDerStelle(int grad) {
		
		double rechtsGrad = berechneUmdrehungenProRunde() * grad;
		double linksGrad = -1 * rechtsGrad;
		
		motorRechts.synchronizeWith(new RegulatedMotor[] { motorLinks });
		motorRechts.startSynchronization();

		
		motorRechts.setSpeed(70);
		motorRechts.rotate((int) rechtsGrad,true);
		
		motorLinks.setSpeed(70);
		motorLinks.rotate((int) linksGrad, true);
		
		motorRechts.endSynchronization();
		motorRechts.waitComplete();
		motorLinks.waitComplete();		
	}
}
