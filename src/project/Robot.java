package project;

import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltaschallSensor;

public class Robot {

	private CommunicationManager Brain;
	private int[] memory;
	private Lichtsensor lichtSensorLinks;
	private Lichtsensor lichtSensorRechts;

	private InfrarotSensor infrarotSensor;

	private UltaschallSensor ultraschallSensor;
	
	

	private Motor motor;

	public Robot(Port rechterMotorPort, Port linkerMotorPort) {
		// this.lichtSensorLinks = new Lichtsensor(lichtPortLinks);
		// this.lichtSensorRechts = new Lichtsensor(lichtPortRechts);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		motor.turnLeft();
		Brain = new CommunicationManager();

		memory = Brain.start();

		LCD.clearDisplay();
		LCD.drawString("sucess", 0, 5);
		
		System.out.println(memory.length);
		if (memory == null){
			Sound.beep();
			System.out.println("shit");
		}
			
		else
			doWhatLemmingsDo();
		
		

	}

	private void doWhatLemmingsDo() {
		System.out.println("Richtig");
		
		for (int i = 0; i < memory.length; i++) {
			System.out.println(memory[i]);
			switch (memory[i]) {
			case 0:
				System.out.println("Case: 0");
				motor.turnLeft();
				break;
			case 1:
				System.out.println("Case: 1");
				motor.turnRight();
				break;

			case 2:
				System.out.println("Case: 2");
				motor.fahreGerade();
				break;

			default:
				System.out.println("Case: else");
				motor.fahreGerade();
				break;
			}
		}

	}

	// public Robot(String irPortNummer) {
	// this.infrarotSensor = new InfrarotSensor(irPortNummer);
	// }

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
