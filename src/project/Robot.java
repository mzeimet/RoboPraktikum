package project;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.HiTechnicEOPD;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;

public class Robot {

	private Lichtsensor lichtSensorLinks;
	private Lichtsensor lichtSensorRechts;

	private InfrarotSensor infrarotSensor;

	private Motor motor;

	public Robot(String lichtPortLinks, String lichtPortRechts, Port linkerMotorPort, Port rechterMotorPort) {
		this.lichtSensorLinks = new Lichtsensor(lichtPortLinks);
		this.lichtSensorRechts = new Lichtsensor(lichtPortRechts);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);

	}

	public Robot(String irPortNummer) {
		this.infrarotSensor = new InfrarotSensor(irPortNummer);
	}

	public boolean checkHinderniss() {
		return infrarotSensor.checktHinderniss();
	}

	public void steheStill() {
		motor.setGeschwindigkeit(0);
	}

	public void DriveForward() {
		SaveMove(Direction.FORWARD);
		motor.fahreGerade();
	}

	private void turnLeft() {
		SaveMove(Direction.LEFT);
		motor.turnLeft();
	}

	private void turnRight() {
		SaveMove(Direction.RIGHT);
		motor.turnRight();
	}

	private void SaveMove(Direction right) {
		// Just save the Direction to send it later

	}

	// /**
	// *
	// * @return Helligkeit in Prozent, 0-100
	// */
	// public int getLichtInProzent(LeftRight lr) {
	// float sample[] = new float[1];
	// if (lr.equals(LEFT)) {
	// lichtSensorLinks.getWert();
	// } else {
	// lichtSensorRechts.getWert();
	// }
	// return new Double(sample[0] * 100.0).intValue();
	// }
}
