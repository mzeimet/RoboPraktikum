package project;

import static project.Direction.LEFT;

import java.util.LinkedList;

import lejos.hardware.port.Port;
import project.motors.MiniMotor;
import project.motors.Motor;

public class Robot {

	private CommunicationManager Brain;
	private LinkedList<Integer> memory = new LinkedList<Integer>();

	private Motor motor;
	private MiniMotor minimotor;

	private static final int GRENZWERT_ABSTAND_WAND_SUCHEN = 50;
	private static final String FEHLER_KEINE_WAND = "404 Wand nicht gefunden :(";
	private static final float GRENZWERT_ABSTAND_WAND_FAHREN = 5;
	private static final float FELDLAENGE = 12;
	private static final float GRENZWERT_ABWEICHUNG_IR = 1f;
	private static final int INTERVALL_GROESSE_IR_MESSUNG = 20;

	private static final float FAHRE_GERADE_DISTANZ = 5f;
	private static final int MAGISCHE_TOLERANZ_KONSTANTE = 1;
	private static final double KONSTANTE_RAD_UMFANG = 5.6f * 3.1415926;

	private float letzterAbstand;

	private boolean zielGefunden = false;

	public Robot(Port rechterMotorPort, Port linkerMotorPort) {
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		Brain = new CommunicationManager();
	}

	public void doWhatLemmingsDo(LinkedList<Integer> memory) {
		System.out.println(getMemory().size());

		for (int i = 0; i < getMemory().size(); i++) {

			int j = getMemory().getFirst();
			switch (j) {
			case 0:// Links
				linksDrehung();
				break;
			case 1:// Rechts
				rechtsDrehung();

			default:
				motor.driveTachoCount(j);
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

	private void drehe(Direction richtung) {
		SaveMove(richtung);
		motor.drehe(richtung);
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

	public void linksDrehung() {
		motor.setGeschwindigkeit(30);
		motor.fahreGerade(1.2);
		drehe(LEFT);
		motor.setGeschwindigkeit(30);
		motor.fahreGerade(1.2 * 0.5);
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

	public void drehe(int grad) {
		motor.drehenAufDerStelle(grad);
	}

	private void rechtsDrehung() {
		// schaue in die Ecke
		motor.drehenAufDerStelle(-45);

		// fahre zurück um Abstand aufzubauen
		// Wert muss ertestet werden
		motor.fahreGerade(-5.2 / KONSTANTE_RAD_UMFANG);

		motor.drehenAufDerStelle(45);

		motor.fahreGerade((0.5) / KONSTANTE_RAD_UMFANG);
		// Drehe zu Wand rechts
		motor.drehenAufDerStelle(90);
	}

	/**
	 * berechnet den Winkel in dem der Roboter relativ zum ausgangswinkel zur
	 * wand steht, abhägig vom ursprünglichen Abstand und Winkel
	 * 
	 * @param aktuellerAbstand
	 * @return
	 */
	private float berechneWinkel(float differenz) {
		float hypothenuse = FAHRE_GERADE_DISTANZ;
		return (float) Math.toDegrees(Math.asin(differenz / hypothenuse));
	}

}
