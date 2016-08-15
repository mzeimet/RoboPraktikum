package project;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.SensorMode;
import static project.LeftRight.*;

public class Robot {

	private static final String AMBIENT_MODE = "Ambient";
	private static final int TOP_SPEED = 740;

	private SensorMode lichtSensorLinks;
	private SensorMode lichtSensorRechts;
	private EV3LargeRegulatedMotor motorLinks;
	private EV3LargeRegulatedMotor motorRechts;

	public Robot(String lichtPortLinks, String lichtPortRechts, Port linkerMotorPort, Port rechterMotorPort) {
		this.lichtSensorLinks = getLichtSensor(lichtPortLinks);
		this.lichtSensorRechts = getLichtSensor(lichtPortRechts);
		this.motorLinks = new EV3LargeRegulatedMotor(linkerMotorPort);
		this.motorRechts = new EV3LargeRegulatedMotor(rechterMotorPort);
	}

	private SensorMode getLichtSensor(String portNummer) {
		Port port = LocalEV3.get().getPort(portNummer);
		NXTLightSensor lichtSensor = new NXTLightSensor(port);
		turnLightsOff(lichtSensor);
		return lichtSensor;
	}

	private void turnLightsOff(NXTLightSensor lichtSensor) {
		lichtSensor.setFloodlight(false);
		lichtSensor.setCurrentMode(AMBIENT_MODE);
		// AMBIENT_MODE == LED aus
	}

	public void setGeneralSpeed(int speedInPercent) {
		setSpeedInPercent(speedInPercent, LEFT);
		setSpeedInPercent(speedInPercent, RIGHT);
	}

	public void setSpeedInPercent(int percent, LeftRight lr) {
		percent = validateOrCorrectPercent(percent);
		if (lr.equals(LEFT)) {
			motorLinks.setSpeed(percent * TOP_SPEED / 100);
			motorLinks.forward();
		} else {
			motorRechts.setSpeed(percent * TOP_SPEED / 100);
			motorRechts.forward();
		}
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

	public void runForward() {
		motorLinks.forward();
		motorRechts.forward();
	}

	/**
	 * 
	 * @return Helligkeit in Prozent, 0-100
	 */
	public int getLigthInPercent(LeftRight lr) {
		float sample[] = new float[1];
		if (lr.equals(LEFT)) {
			lichtSensorLinks.fetchSample(sample, 0);
		} else {
			lichtSensorRechts.fetchSample(sample, 0);
		}
		return new Double(sample[0] * 100.0).intValue();
	}

	public void standStill() {
		setGeneralSpeed(0);
	}
}
