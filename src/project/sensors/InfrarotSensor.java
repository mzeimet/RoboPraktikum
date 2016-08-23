package project.sensors;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.robotics.objectdetection.FeatureListener;

public class InfrarotSensor {
	
	private HiTechnicEOPD sensor;
	
	private static final float SCHWELLWERT = 99.5f;

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
		float wert = getWert();
		return getWert() < SCHWELLWERT;
	}
	
	public float messeAbstand(){
		float wert = 100f -getWert();
		if(wert > 5.2){
			return 0; // zu nah
		}
		if(wert  < 0.2 ){
			return 15; // zu weit
		}
		if(wert <2.2){
			return 15 - wert/0.3f;
		} else	{
			if(wert > 3.8){
				return 4;
			}
			if(wert >2.8){
				return 5;
			}
			else{
				return 6;
			}
		}
	}
}
