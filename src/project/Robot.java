package project;

import java.util.LinkedList;

import lejos.hardware.port.Port;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltaschallSensor;

public class Robot {

	private CommunicationManager Brain;
	private LinkedList<Integer> memory = new LinkedList<Integer>();

	private Lichtsensor lichtSensorLinks;
	private Lichtsensor lichtSensorRechts;
	private InfrarotSensor infrarotSensor;
	private UltaschallSensor ultraschallSensor;

	private Motor motor;

	public Robot(Port rechterMotorPort, Port linkerMotorPort) {
		// this.lichtSensorLinks = new Lichtsensor(lichtPortLinks);
		// this.lichtSensorRechts = new Lichtsensor(lichtPortRechts);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		Brain = new CommunicationManager();
		// hier kommt Marvins verrückter Algorithmus hin

	}

	/*
	 * Folgt stupide den Schritten des vorherigen Robots
	 */
	public void doWhatLemmingsDo(LinkedList<Integer> memory) {
		System.out.println(getMemory().size());

		for (int i = 0; i < getMemory().size(); i++) {
			System.out.println(getMemory().getFirst());
			getMemory().removeFirst();
			switch (getMemory().getFirst()) {
			case 0:
				System.out.println("Case: 0");
				motor.fahreGerade();
				break;
			case 1:
				System.out.println("Case: 1");
				motor.turnLeft();
				break;

			case 2:
				System.out.println("Case: 2");
				motor.turnRight();
				break;

			default:
				System.out.println("Case: else");
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

	private void SaveMove(Direction dir) {
		switch (dir) {
		case FORWARD:
			getMemory().addFirst(0);
			break;
		case LEFT:
			getMemory().addFirst(1);
			break;
		case RIGHT:
			getMemory().addFirst(2);
			break;

		default:
			break;
		}

	}

	public LinkedList<Integer> getMemory() {
		return memory;
	}

	public void setMemory(LinkedList<Integer> memory) {
		this.memory = memory;
	}

	public CommunicationManager getBrain() {
		return Brain;
	}

	public void setBrain(CommunicationManager brain) {
		Brain = brain;
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
