package project.sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.HiTechnicEOPD;

public class InfrarotSensor {
	
	private HiTechnicEOPD sensor;
	
	private static final float SCHWELLWERT = 98;

	public InfrarotSensor(String irPortNummer) {
		Port port = LocalEV3.get().getPort(irPortNummer);
		this.sensor = new HiTechnicEOPD(port);
		sensor.getShortDistanceMode();
	}

	public float getWert(){
		float sample[] = new float[1];
		sensor.fetchSample(sample, 0);
		return sample[0]*100;
	}
	
	public boolean checktHinderniss(){
		return getWert() < SCHWELLWERT;
	}
	
}
