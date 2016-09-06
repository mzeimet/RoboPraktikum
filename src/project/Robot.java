package project;

import static project.Direction.FORWARD;
import static project.Direction.LEFT;
import static project.Direction.RIGHT;

import java.util.LinkedList;

import lejos.hardware.port.Port;
import project.motors.MiniMotor;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltraschallSensor;

public class Robot {

	private CommunicationManager Brain;
	private LinkedList<Integer> memory = new LinkedList<Integer>();

	private Lichtsensor lichtSensor;

	private InfrarotSensor infrarotSensorVorne;
	private InfrarotSensor infrarotSensorHinten;

	private UltraschallSensor ultraschallSensor;

	private Motor motor;
	private MiniMotor minimotor;

	private static final int GRENZWERT_ABSTAND_WAND_SUCHEN = 50;
	private static final String FEHLER_KEINE_WAND = "404 Wand nicht gefunden :(";
	private static final float GRENZWERT_ABSTAND_WAND_FAHREN = 5;
	private static final float FELDLAENGE = 12;
	private static final float GRENZWERT_ABWEICHUNG_IR = 1f;
	private static final int INTERVALL_GROESSE_IR_MESSUNG = 10;

	private static final float FAHRE_GERADE_DISTANZ = 5f;
	private static final int MAGISCHE_TOLERANZ_KONSTANTE = 1;
	private static final double KONSTANTE_RAD_UMFANG = 5.6f * 3.1415926;
	private static final int ABSTAND_IR_SENSOREN = 0; // TODO
	private static final int TOLERANZ_DIFF_IR = 2;

	private float letzterAbstand;

	private boolean zielGefunden = false;

	public Robot(String usPort, String irPortVorne, String irPortHinten, Port miniMotorPort, Port linkerMotorPort,
			Port rechterMotorPort, String lichtPort) {
		this.ultraschallSensor = new UltraschallSensor(usPort);
		this.minimotor = new MiniMotor(miniMotorPort);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		this.infrarotSensorVorne = new InfrarotSensor(irPortVorne);
		this.infrarotSensorHinten = new InfrarotSensor(irPortHinten);
		this.lichtSensor = new Lichtsensor(lichtPort);
		Brain = new CommunicationManager();
	}

	public void dreheInfrarotSensor(Direction richtung) {
		minimotor.drehe(richtung);
	}

	public void steheStill() {
		motor.setGeschwindigkeit(0);
	}

	private void drehe(Direction richtung) {
		if (richtung.equals(RIGHT)) {
			SaveMove(1);
		} else {
			SaveMove(0);
		}
		motor.drehe(richtung);
	}

	public float getUltraschallAbstand() {
		return ultraschallSensor.getAbstandInCm();
	}

	private void SaveMove(int dir) {
		if (dir < 0)
			throw new IllegalArgumentException();
		switch (dir) {
		case 0: // LEFT
			getMemory().addFirst(1);
			break;
		case 1: // RIGHT
			getMemory().addFirst(2);
			break;
		default: // FORWARD
			// tanze im Kreis und singe ein lied TODO Steven
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

	public void drehe(int grad) {
		motor.drehenAufDerStelle(grad);
	}

	public void findeWand() {
		try {
			fahreZuWand();
			dreheZuWand();
			try {
				letzterAbstand = messeAbstand();
			} catch (Exception e) {
			}
			while (!zielGefunden) {
				folgeWand();
				if (!checkeHindernisInfrarot(LEFT)) {
					// links frei
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(4);
					steheStill();
					drehe(LEFT);
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(4);
					steheStill();
				} else { // links hinderniss
					if (checkeHindernisInfrarot(RIGHT)) {
						// sackgasse
						drehe(RIGHT);
						drehe(RIGHT);
					} else {
						drehe(Direction.RIGHT);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dreheZuWand() {
		int gradBeiMin = 0;
		minimotor.dreheZurueck();
		float min = Float.MAX_VALUE;
		for (int aktGradZahl = -minimotor.getMaxGradzahl(); aktGradZahl <= minimotor
				.getMaxGradzahl(); aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensorVorne.messeAbstand();
			if (abstand < min) {
				min = abstand;
				gradBeiMin = aktGradZahl;
			}
		}
		minimotor.dreheZurueck();
		motor.drehenAufDerStelle(gradBeiMin);
		drehe(RIGHT);
	}

	/**
	 * Folgt der linken Wand bis diese nicht mehr da ist oder er vor einem
	 * Hindernis steht
	 */
	public void folgeWand() {
		int tachoCount = motor.getTachoCount();
		boolean linksKeineWand = false;
		boolean stehtVorHinderniss = false;
		boolean darfFahren = true;
		linksKeineWand = !checkeHindernisInfrarot(LEFT);
		stehtVorHinderniss = checkeHindernisUltraschall();
		if (stehtVorHinderniss) {
			stehtVorHinderniss = pruefeUltraschallMitInfrarot();
		}
		darfFahren = !linksKeineWand && !stehtVorHinderniss;
		if (darfFahren) {
			letzterAbstand = messeAbstand(0);
			fahre();
		}
		while (darfFahren) {
			linksKeineWand = !checkeHindernisInfrarot(LEFT);
			stehtVorHinderniss = checkeHindernisUltraschall();
			if (stehtVorHinderniss) {
				stehtVorHinderniss = pruefeUltraschallMitInfrarot();
			}
			darfFahren = !linksKeineWand && !stehtVorHinderniss && abstandStimmt();
		}
		motor.stop();
		tachoCount = motor.getTachoCount() - tachoCount; // TODO
		SaveMove(tachoCount);
	}

	private boolean abstandStimmt() {
		if (Math.abs(messeAbstand(0) - letzterAbstand) > MAGISCHE_TOLERANZ_KONSTANTE) {
			int diff = Math.abs(messeAbstand(0) - messeAbstand(1)) - ABSTAND_IR_SENSOREN;
			if (diff > TOLERANZ_DIFF_IR) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Prueft ob der US-sensor richtige werte liefert, noch zu impelenieren TODO
	 * 
	 * @return
	 */
	private boolean pruefeUltraschallMitInfrarot() {
		return checkeHindernisInfrarot(FORWARD);
	}

	public void fahre() {
		motor.setGeschwindigkeit(30);
		motor.fahreGerade();
	}

	/**
	 * Misst den Abstand des IR-Sensors zum nächstmöglichen Objekt
	 * 
	 * @return
	 * @throws Exception:
	 *             Geringster Abstand wurde an einer messgrenze (+-
	 *             miniMotor.MAX_GRADZAHL) gemessen, d.h. korrektheit des wertes
	 *             ist nicht mehr garantiert
	 */
	public float messeAbstand() throws Exception {
		float min = Float.MAX_VALUE;
		boolean groessterWinkel = true;
		for (int aktGradZahl = -minimotor
				.getMaxGradzahl(); aktGradZahl <= 0 /*
													 * minimotor
													 * .getMaxGradzahl()
													 */; aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensor.messeAbstand();
			if (abstand < min) {
				min = abstand;
				if (Math.abs(aktGradZahl) < minimotor.getMaxGradzahl()) {
					groessterWinkel = false;
				}
			}
		}
		minimotor.dreheZurueck();

		return min;
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

	private void korregiereAbstand() {
		float aktuellerAbstand = 0;
		try {
			aktuellerAbstand = messeAbstand();
		} catch (Exception e) {
			motor.drehenAufDerStelle(-20);
			korregiereAbstand();
			return;
		}

		if (letzterAbstand < aktuellerAbstand + MAGISCHE_TOLERANZ_KONSTANTE
				&& letzterAbstand > aktuellerAbstand - MAGISCHE_TOLERANZ_KONSTANTE)
			return;

		float differenz = letzterAbstand - aktuellerAbstand;
		float winkel = berechneWinkel(differenz);

		motor.drehenAufDerStelle((int) winkel);
		motor.drehenAufDerStelle(-90);
		motor.fahreGerade((double) -differenz / KONSTANTE_RAD_UMFANG);
		motor.drehenAufDerStelle(90);

	}

	private void fahreZuWand() {
		motor.setGeschwindigkeit(30);
		boolean nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		while (nichtErreicht) {
			SaveMove(FORWARD);
			motor.fahreGerade(1);
			nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		}
		steheStill();
	}

	public boolean checkeHindernisInfrarot(Direction richtung) {
		minimotor.drehe(richtung);
		boolean hasHindernis = infrarotSensor.checktHinderniss();
		minimotor.drehe(FORWARD);
		return hasHindernis;
	}

	public boolean checkeHindernisUltraschall() {
		float abstand = ultraschallSensor.getAbstandInCm();
		return abstand < GRENZWERT_ABSTAND_WAND_FAHREN;
	}

	public void drehenAufDerStelle() {
		motor.drehenAufDerStelle(-90);
	}

}
