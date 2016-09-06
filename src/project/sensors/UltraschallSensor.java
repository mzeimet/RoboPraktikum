package project.sensors;

import static project.Config.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class UltraschallSensor {

	private EV3UltrasonicSensor sensor;

	public UltraschallSensor(String usPortNummer) {
		Port port = LocalEV3.get().getPort(usPortNummer);
		this.sensor = new EV3UltrasonicSensor(port);
		sensor.getDistanceMode();
	}
	
	/**
	 * Abstand in cm
	 */
	public float getAbstandInCm(){
		float sample[] = new float[1];
		sensor.fetchSample(sample, 0);
		return sample[0]*100;
	}
	
}
