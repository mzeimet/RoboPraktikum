package project.sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.NXTLightSensor;

public class Lichtsensor {

	private static final String AMBIENT_MODE = "Ambient";

	private NXTLightSensor sensor;

	public Lichtsensor(String portNummer) {
		Port port = LocalEV3.get().getPort(portNummer);
		this.sensor = new NXTLightSensor(port);
		turnLightsOff();
	}

	private void turnLightsOff() {
		sensor.setFloodlight(false);
		sensor.setCurrentMode(AMBIENT_MODE);
		// AMBIENT_MODE == LED aus
	}

	public float getWert() {
		float sample[] = new float[1];
		sensor.fetchSample(sample, 0);
		return sample[0];
	}

}
